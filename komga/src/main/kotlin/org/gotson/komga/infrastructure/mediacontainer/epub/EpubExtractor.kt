package org.gotson.komga.infrastructure.mediacontainer.epub

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.compress.archivers.ArchiveEntry
import org.gotson.komga.domain.model.BookPage
import org.gotson.komga.domain.model.EpubTocEntry
import org.gotson.komga.domain.model.MediaFile
import org.gotson.komga.domain.model.R2Locator
import org.gotson.komga.domain.model.TypedBytes
import org.gotson.komga.infrastructure.image.ImageAnalyzer
import org.gotson.komga.infrastructure.kobo.KepubConverter
import org.gotson.komga.infrastructure.mediacontainer.ContentDetector
import org.gotson.komga.infrastructure.util.getEntryBytes
import org.gotson.komga.infrastructure.util.getEntryInputStream
import org.gotson.komga.infrastructure.util.getZipEntryBytes
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.util.UriUtils
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.math.roundToInt

private val logger = KotlinLogging.logger {}

@Service
class EpubExtractor(
  private val contentDetector: ContentDetector,
  private val imageAnalyzer: ImageAnalyzer,
  private val kepubConverter: KepubConverter,
  @Value("#{@komgaProperties.epubDivinaLetterCountThreshold}") private val letterCountThreshold: Int,
) {
  /**
   * Retrieves a specific entry by name from the zip archive
   */
  fun getEntryStream(
    path: Path,
    entryName: String,
  ): ByteArray = getZipEntryBytes(path, entryName)

  fun isEpub(path: Path): Boolean =
    try {
      getEntryStream(path, "mimetype").decodeToString().trim() == "application/epub+zip"
    } catch (e: Exception) {
      false
    }

  /**
   * Retrieves the book cover along with its mediaType from the epub 2/3 manifest
   */
  fun getCover(path: Path): TypedBytes? =
    path.epub { (zip, opfDoc, opfDir, manifest) ->
      val coverManifestItem =
        // EPUB 3 - try to get cover from manifest properties 'cover-image'
        manifest.values.firstOrNull { it.properties.contains("cover-image") }
          ?: // EPUB 2 - get cover from meta element with name="cover"
          opfDoc
            .selectFirst("metadata > meta[name=cover]")
            ?.attr("content")
            ?.ifBlank { null }
            ?.let { manifest[it] }
          ?: // try id="cover-image"
          manifest.values.firstOrNull { it.id == "cover-image" }
      if (coverManifestItem != null) {
        val href = coverManifestItem.href
        val mediaType = coverManifestItem.mediaType
        val coverPath = normalizeHref(opfDir, href)
        zip.getEntryBytes(coverPath)?.let { coverBytes ->
          TypedBytes(
            coverBytes,
            mediaType,
          )
        }
      } else {
        null
      }
    }

  fun getManifest(
    path: Path,
    analyzeDimensions: Boolean,
  ): EpubManifest =
    path.epub { epub ->
      val (resources, missingResources) = getResources(epub).partition { it.fileSize != null }
      val isFixedLayout = isFixedLayout(epub)
      val pageCount = computePageCount(epub)
      val isKepub = isKepub(epub, resources)
      EpubManifest(
        resources = resources,
        missingResources = missingResources,
        toc = getToc(epub),
        landmarks = getLandmarks(epub),
        pageList = getPageList(epub),
        pageCount = pageCount,
        isFixedLayout = isFixedLayout,
        positions = computePositions(epub, path, resources, isFixedLayout, isKepub),
        divinaPages = getDivinaPages(epub, isFixedLayout, pageCount, analyzeDimensions),
        isKepub = isKepub,
      )
    }

  private fun getResources(epub: EpubPackage): List<MediaFile> {
    val spine =
      epub.opfDoc
        .select("spine > itemref")
        .map { it.attr("idref") }
        .mapNotNull { epub.manifest[it] }

    val pages =
      spine.map { page ->
        MediaFile(
          normalizeHref(epub.opfDir, UriUtils.decode(page.href, Charsets.UTF_8)),
          page.mediaType,
          MediaFile.SubType.EPUB_PAGE,
        )
      }

    val assets =
      epub.manifest.values.filterNot { spine.contains(it) }.map {
        MediaFile(
          normalizeHref(epub.opfDir, UriUtils.decode(it.href, Charsets.UTF_8)),
          it.mediaType,
          MediaFile.SubType.EPUB_ASSET,
        )
      }

    val zipEntries = epub.zip.entries.toList()
    return (pages + assets).map { resource ->
      resource.copy(fileSize = zipEntries.firstOrNull { it.name == resource.fileName }?.let { if (it.size == ArchiveEntry.SIZE_UNKNOWN) null else it.size })
    }
  }

  private fun getDivinaPages(
    epub: EpubPackage,
    isFixedLayout: Boolean,
    pageCount: Int,
    analyzeDimensions: Boolean,
  ): List<BookPage> {
    if (!isFixedLayout) {
      logger.info { "Epub Divina detection failed: book is not fixed layout" }
      return emptyList()
    }

    try {
      val pagesWithImages =
        epub.opfDoc
          .select("spine > itemref")
          .map { it.attr("idref") }
          .mapNotNull { idref -> epub.manifest[idref]?.href?.let { normalizeHref(epub.opfDir, it) } }
          .map { pagePath ->
            val doc = epub.zip.getEntryInputStream(pagePath)?.use { Jsoup.parse(it, null, "") } ?: return@map emptyList()

            // if a page has text over the threshold then the book is not divina compatible
            if (doc.body().text().length > letterCountThreshold) return emptyList()

            val img =
              doc
                .getElementsByTag("img")
                .map { it.attr("src") } // get the src, which can be a relative path

            val svg =
              doc
                .select("svg > image[xlink:href]")
                .map { it.attr("xlink:href") } // get the source, which can be a relative path

            (img + svg).map { (Path(pagePath).parent ?: Path("")).resolve(it).normalize().invariantSeparatorsPathString } // resolve it against the page folder
          }

      if (pagesWithImages.size != pageCount) {
        logger.info { "Epub Divina detection failed: book has ${pagesWithImages.size} pages with images, but $pageCount total pages" }
        return emptyList()
      }
      // Only keep unique image path for each page. KCC sometimes generates HTML pages with 5 times the same image.
      val imagesPath = pagesWithImages.map { it.distinct() }.flatten()
      if (imagesPath.size != pageCount) {
        logger.info { "Epub Divina detection failed: book has ${imagesPath.size} detected images, but $pageCount total pages" }
        return emptyList()
      }

      val divinaPages =
        imagesPath.mapNotNull { imagePath ->
          val mediaType =
            epub.manifest.values
              .firstOrNull { normalizeHref(epub.opfDir, it.href) == imagePath }
              ?.mediaType ?: return@mapNotNull null
          val zipEntry = epub.zip.getEntry(imagePath)
          if (!contentDetector.isImage(mediaType)) return@mapNotNull null

          val dimension =
            if (analyzeDimensions)
              epub.zip.getInputStream(zipEntry).use { imageAnalyzer.getDimension(it) }
            else
              null
          val fileSize = if (zipEntry.size == ArchiveEntry.SIZE_UNKNOWN) null else zipEntry.size
          BookPage(fileName = imagePath, mediaType = mediaType, dimension = dimension, fileSize = fileSize)
        }

      if (divinaPages.size != pageCount) {
        logger.info { "Epub Divina detection failed: book has ${divinaPages.size} detected divina pages, but $pageCount total pages" }
        return emptyList()
      }
      return divinaPages
    } catch (e: Exception) {
      logger.warn(e) { "Error while getting divina pages" }
      return emptyList()
    }
  }

  private fun isKepub(
    epub: EpubPackage,
    resources: List<MediaFile>,
  ): Boolean {
    try {
      val readingOrder = resources.filter { it.subType == MediaFile.SubType.EPUB_PAGE }

      readingOrder.forEach { mediaFile ->
        val doc = epub.zip.getEntryInputStream(mediaFile.fileName)?.use { Jsoup.parse(it, null, "") }
        if (!doc?.getElementsByClass("koboSpan").isNullOrEmpty()) return true
      }
    } catch (e: Exception) {
      logger.warn(e) { "Error while checking if EPUB is KEPUB" }
    }
    return false
  }

  private fun computePageCount(epub: EpubPackage): Int {
    val spine =
      epub.opfDoc
        .select("spine > itemref")
        .map { it.attr("idref") }
        .mapNotNull { idref -> epub.manifest[idref]?.href?.let { normalizeHref(epub.opfDir, it) } }

    return epub.zip.entries
      .toList()
      .filter { it.name in spine }
      .sumOf { ceil(it.compressedSize / 1024.0).toInt() }
  }

  private fun isFixedLayout(epub: EpubPackage) =
    epub.opfDoc.selectFirst("metadata > *|meta[property=rendition:layout]")?.text() == "pre-paginated" ||
      epub.opfDoc.selectFirst("metadata > *|meta[name=fixed-layout]")?.attr("content") == "true"

  private fun computePositions(
    epub: EpubPackage,
    path: Path,
    resources: List<MediaFile>,
    isFixedLayout: Boolean,
    isKepub: Boolean,
  ): List<R2Locator> {
    val readingOrder = resources.filter { it.subType == MediaFile.SubType.EPUB_PAGE }

    var startPosition = 1

    val koboPositions =
      when {
        isFixedLayout -> emptyMap()
        isKepub -> computePositionsFromKoboSpan(readingOrder) { filename -> epub.zip.getEntryInputStream(filename).use { it?.readBytes()?.decodeToString() } }
        kepubConverter.isAvailable -> {
          try {
            val kepub =
              kepubConverter
                .convertEpubToKepubWithoutChecks(path)
                ?.also { it.toFile().deleteOnExit() }
                // if the conversion failed, throw an exception that will be caught in the catch block
                ?: throw IllegalStateException()
            val positions = computePositionsFromKoboSpan(readingOrder) { filename -> getZipEntryBytes(kepub, filename).decodeToString() }
            kepub.deleteIfExists()
            positions
          } catch (e: Exception) {
            logger.warn { "Could not convert to Kepub to compute positions: $path" }
            emptyMap()
          }
        }

        else -> emptyMap()
      }

    val positions =
      if (isFixedLayout) {
        // for fixed-layout book we create 1 position per page
        readingOrder.map {
          R2Locator(
            href = it.fileName,
            type = it.mediaType ?: "application/octet-stream",
            koboSpan = "kobo.1.1",
            locations = R2Locator.Location(progression = 0F, position = startPosition++),
          )
        }
      } else {
        // this is the Readium algorithm
        // we create 1 position every 1024 bytes
        readingOrder.flatMap { file ->
          val positionCount = maxOf(1, ceil((file.fileSize ?: 0) / 1024.0).roundToInt())
          (0 until positionCount).map { p ->
            val progression = p.toFloat() / positionCount
            val koboSpan =
              if (positionCount == 1 || p == 0)
                "kobo.1.1"
              else
                koboPositions[file.fileName]
                  ?.minByOrNull { (progression - it.second).absoluteValue }
                  ?.first

            R2Locator(
              href = file.fileName,
              type = file.mediaType ?: "application/octet-stream",
              locations = R2Locator.Location(progression = progression, position = startPosition++),
              koboSpan = koboSpan,
            )
          }
        }
      }

    // finally we compute the total progression for each position
    return positions.map { locator ->
      val totalProgression = locator.locations?.position?.let { it.toFloat() / positions.size }
      locator.copy(locations = locator.locations?.copy(totalProgression = totalProgression))
    }
  }

  /**
   * Builds the positions for a KEPUB book, based on koboSpan tags.
   * @return a [Map] where the key is the resource name, and the value is a [List] of [Pair] containing the koboSpan ID and the progression as a Float between 0 and 1.
   */
  private fun computePositionsFromKoboSpan(
    readingOrder: List<MediaFile>,
    resourceSupplier: (String) -> String?,
  ): Map<String, List<Pair<String, Float>>?> =
    readingOrder.associate { file ->
      val doc = resourceSupplier(file.fileName)?.let { resource -> Jsoup.parse(resource, Parser.htmlParser().setTrackPosition(true)) }
      file.fileName to
        doc?.select("span.koboSpan")?.mapNotNull { koboSpan ->
          val id = koboSpan.id()
          if (!id.isNullOrBlank()) {
            // progression is built from the position in the file of each koboSpan, divided by the file size
            val progression = koboSpan.sourceRange().endPos().toFloat() / file.fileSize!!.toFloat()
            Pair(id, progression)
          } else {
            null
          }
        }
    }

  private fun getToc(epub: EpubPackage): List<EpubTocEntry> {
    // Epub 3
    epub.getNavResource()?.let { return processNav(it, Epub3Nav.TOC) }
    // Epub 2
    epub.getNcxResource()?.let { return processNcx(it, Epub2Nav.TOC) }
    return emptyList()
  }

  private fun getPageList(epub: EpubPackage): List<EpubTocEntry> {
    // Epub 3
    epub.getNavResource()?.let { return processNav(it, Epub3Nav.PAGELIST) }
    // Epub 2
    epub.getNcxResource()?.let { return processNcx(it, Epub2Nav.PAGELIST) }
    return emptyList()
  }

  private fun getLandmarks(epub: EpubPackage): List<EpubTocEntry> {
    // Epub 3
    epub.getNavResource()?.let { return processNav(it, Epub3Nav.LANDMARKS) }

    // Epub 2
    return processOpfGuide(epub.opfDoc, epub.opfDir)
  }
}
