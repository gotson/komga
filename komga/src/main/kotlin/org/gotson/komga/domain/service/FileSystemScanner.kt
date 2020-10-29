package org.gotson.komga.domain.service

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import mu.KotlinLogging
import org.apache.commons.io.FilenameUtils
import org.gotson.komga.domain.model.*
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.gotson.komga.infrastructure.mediacontainer.MediaContainerExtractor
import org.gotson.komga.infrastructure.metadata.comicinfo.dto.ComicInfo
import org.gotson.komga.infrastructure.web.filePathToUrl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.IOException
import java.nio.file.FileVisitOption
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.time.measureTime

private val logger = KotlinLogging.logger {}
private const val COMIC_INFO = "ComicInfo.xml"


@Service
class FileSystemScanner(
  private val komgaProperties: KomgaProperties,
  extractors: List<MediaContainerExtractor>,
  private val bookAnalyzer: BookAnalyzer
) {

  @Autowired(required = false) private val mapper: XmlMapper = XmlMapper()
  val supportedExtensions = listOf("cbz", "zip", "cbr", "rar", "pdf", "epub")
  val supportedMediaTypes = extractors
    .flatMap { e -> e.mediaTypes().map { it to e } }
    .toMap()

  fun scanRootFolder(root: Path, forceDirectoryModifiedTime: Boolean = false): Map<Series, List<Book>> {
    logger.info { "Scanning folder: $root" }
    logger.info { "Supported extensions: $supportedExtensions" }
    logger.info { "Excluded patterns: ${komgaProperties.librariesScanDirectoryExclusions}" }
    logger.info { "Force directory modified time: $forceDirectoryModifiedTime" }

    var scannedSeries: MutableMap<Series, List<Book>>

    measureTime {
      val dirs = mutableListOf<Path>()
      var bookfile: Book
      var seriesfile: Series
      var mediabook: Media
      var seriesName: String

      scannedSeries = mutableMapOf()

      Files.walkFileTree(root, setOf(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, object : FileVisitor<Path> {
        override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes?): FileVisitResult {
          logger.trace { "preVisit: $dir" }
          if (!Files.isHidden(dir) && komgaProperties.librariesScanDirectoryExclusions.any { exclude ->
              dir.toString().contains(exclude, true)
            }) return FileVisitResult.SKIP_SUBTREE

          dirs.add(dir)
          return FileVisitResult.CONTINUE
        }

        override fun visitFile(file: Path, attrs: BasicFileAttributes?): FileVisitResult {
          if (Files.isReadable(file) && Files.isRegularFile(file) && supportedExtensions.contains(FilenameUtils.getExtension(file.fileName.toString()).toLowerCase())) {
            logger.trace { "File Detected: $file" }
            bookfile = Book(
              FilenameUtils.getBaseName(file.fileName.toString()),
              file.toUri().toURL(),
              file.getUpdatedTime(),
              Files.readAttributes(file, BasicFileAttributes::class.java).size()
            )

            mediabook = bookAnalyzer.analyze(bookfile)

            if ( mediabook.files.none { it == COMIC_INFO } ) {
              logger.debug { "Book does not contain any $COMIC_INFO file: ${bookfile.name}" }
              seriesfile = Series (
                name = dirs.last().fileName.toString(),
                url = dirs.last().toUri().toURL(),
                fileLastModified = dirs.last().getUpdatedTime()
              )
            } else if (mediabook.status != Media.Status.READY) {
              logger.warn { "Book media is not ready, cannot get files" }
              seriesfile = Series (
                name = dirs.last().fileName.toString(),
                url = dirs.last().toUri().toURL(),
                fileLastModified = dirs.last().getUpdatedTime()
              )
            } else {
              val fileContent = supportedMediaTypes.getValue(mediabook.mediaType!!).getEntryStream(bookfile.path(), COMIC_INFO)
              val mtdComic = mapper.readValue(fileContent, ComicInfo::class.java)

              seriesName = mtdComic.series.toString()
              seriesfile = Series(seriesName, filePathToUrl("/$seriesName"), LocalDateTime.now())
            }
              scannedSeries = addBookSeriesMap(seriesfile, bookfile, scannedSeries)
              logger.debug { "Scanned up to now: $scannedSeries" }

          }
          return FileVisitResult.CONTINUE
        }

        override fun visitFileFailed(file: Path?, exc: IOException?): FileVisitResult {
          logger.warn { "Could not access: $file" }
          return FileVisitResult.SKIP_SUBTREE
        }

        override fun postVisitDirectory(dir: Path?, exc: IOException?): FileVisitResult = FileVisitResult.CONTINUE
      })

      return scannedSeries.toMap()
    }
  }

  fun addBookSeriesMap(series: Series, book: Book, scannedSeries: MutableMap<Series, List<Book>>): MutableMap<Series, List<Book>> {
    val nameSearch = series.name
    var found = false
    var bookList: MutableList<Book>

    scannedSeries.forEach { (t, _) ->
      logger.debug { "Comparing: ${t.name} with $nameSearch" }
      if (found) return@forEach
      if ( t.name == nameSearch ){
        logger.debug { "Comparation matched" }
        bookList = scannedSeries[t]?.toMutableList()!!
        bookList.add(book)
        scannedSeries[t] = bookList.toList()
        found = true
      }
    }

    if (!found) {
      logger.debug { "Adding new Serie: $nameSearch" }
      scannedSeries[series] = listOf(book)
    }

    return scannedSeries
  }

  fun Path.getUpdatedTime(): LocalDateTime =
    Files.readAttributes(this, BasicFileAttributes::class.java).let { b ->
      maxOf(b.creationTime(), b.lastModifiedTime()).toLocalDateTime()
        .also { logger.trace { "Get updated time for file $this. Creation time: ${b.creationTime()}, Last Modified Time: ${b.lastModifiedTime()}. Choosing the max (Local Time): $it" } }
    }

  fun FileTime.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(this.toInstant(), ZoneId.systemDefault())

}
