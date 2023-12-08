package org.gotson.komga.infrastructure.mediacontainer.epub

import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import org.gotson.komga.domain.model.EpubTocEntry
import org.gotson.komga.domain.model.MediaFile
import org.gotson.komga.domain.model.TypedBytes
import org.springframework.stereotype.Service
import java.nio.file.Path
import kotlin.math.ceil

@Service
class EpubExtractor {

  /**
   * Retrieves a specific entry by name from the zip archive
   */
  fun getEntryStream(path: Path, entryName: String): ByteArray =
    ZipFile(path.toFile()).use { zip ->
      zip.getInputStream(zip.getEntry(entryName)).use { it.readBytes() }
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
          opfDoc.selectFirst("metadata > meta[name=cover]")?.attr("content")?.ifBlank { null }?.let { manifest[it] }

      if (coverManifestItem != null) {
        val href = coverManifestItem.href
        val mediaType = coverManifestItem.mediaType
        val coverPath = normalizeHref(opfDir, href)
        TypedBytes(
          zip.getInputStream(zip.getEntry(coverPath)).readAllBytes(),
          mediaType,
        )
      } else null
    }

  fun getManifest(path: Path): EpubManifest =
    path.epub { epub ->
      EpubManifest(
        resources = getResources(epub),
        toc = getToc(epub),
        landmarks = getLandmarks(epub),
        pageList = getPageList(epub),
        pageCount = computePageCount(epub),
      )
    }

  private fun getResources(epub: EpubPackage): List<MediaFile> {
    val spine = epub.opfDoc.select("spine > itemref").map { it.attr("idref") }.mapNotNull { epub.manifest[it] }

    val pages = spine.map { page ->
      MediaFile(
        normalizeHref(epub.opfDir, page.href),
        page.mediaType,
        MediaFile.SubType.EPUB_PAGE,
      )
    }

    val assets = epub.manifest.values.filterNot { spine.contains(it) }.map {
      MediaFile(
        normalizeHref(epub.opfDir, it.href),
        it.mediaType,
        MediaFile.SubType.EPUB_ASSET,
      )
    }

    val zipEntries = epub.zip.entries.toList()
    return (pages + assets).map { resource ->
      resource.copy(fileSize = zipEntries.firstOrNull { it.name == resource.fileName }?.let { if (it.size == ArchiveEntry.SIZE_UNKNOWN) null else it.size })
    }
  }

  private fun computePageCount(epub: EpubPackage): Int {
    val spine = epub.opfDoc.select("spine > itemref")
      .map { it.attr("idref") }
      .mapNotNull { idref -> epub.manifest[idref]?.href?.let { normalizeHref(epub.opfDir, it) } }

    return epub.zip.entries.toList().filter { it.name in spine }.sumOf { ceil(it.compressedSize / 1024.0).toInt() }
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
