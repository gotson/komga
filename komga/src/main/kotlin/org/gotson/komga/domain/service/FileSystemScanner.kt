package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.apache.commons.io.FilenameUtils
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.Series
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.springframework.stereotype.Service
import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.streams.asSequence
import kotlin.time.measureTime

private val logger = KotlinLogging.logger {}

@Service
class FileSystemScanner(
  private val komgaProperties: KomgaProperties
) {

  val supportedExtensions = listOf("cbz", "zip", "cbr", "rar", "pdf", "epub")

  fun scanRootFolder(root: Path): Map<Series, List<Book>> {
    logger.info { "Scanning folder: $root" }
    logger.info { "Supported extensions: $supportedExtensions" }
    logger.info { "Excluded patterns: ${komgaProperties.librariesScanDirectoryExclusions}" }
    if (komgaProperties.filesystemScannerForceDirectoryModifiedTime)
      logger.info { "Force directory modified time: active" }

    lateinit var scannedSeries: Map<Series, List<Book>>

    measureTime {
      scannedSeries = Files.walk(root, FileVisitOption.FOLLOW_LINKS).use { dirsStream ->
        dirsStream.asSequence()
          .onEach { logger.trace { "GetSeries file: $it" } }
          .filter { !Files.isHidden(it) }
          .filter { Files.isDirectory(it) }
          .filter { path ->
            komgaProperties.librariesScanDirectoryExclusions.none { exclude ->
              path.toString().contains(exclude, true)
            }
          }
          .mapNotNull { dir ->
            logger.debug { "Processing directory: $dir" }
            val books = Files.list(dir).use { dirStream ->
              dirStream.asSequence()
                .onEach { logger.trace { "GetBooks file: $it" } }
                .filter { Files.isRegularFile(it) }
                .filter { supportedExtensions.contains(FilenameUtils.getExtension(it.fileName.toString()).toLowerCase()) }
                .map {
                  logger.debug { "Processing file: $it" }
                  Book(
                    name = FilenameUtils.getBaseName(it.fileName.toString()),
                    url = it.toUri().toURL(),
                    fileLastModified = it.getUpdatedTime(),
                    fileSize = Files.readAttributes(it, BasicFileAttributes::class.java).size()
                  )
                }.toList()
            }
            if (books.isNullOrEmpty()) {
              logger.debug { "No books in directory: $dir" }
              return@mapNotNull null
            }
            Series(
              name = dir.fileName.toString(),
              url = dir.toUri().toURL(),
              fileLastModified =
              if (komgaProperties.filesystemScannerForceDirectoryModifiedTime)
                maxOf(dir.getUpdatedTime(), books.map { it.fileLastModified }.max()!!)
              else dir.getUpdatedTime()
            ) to books
          }.toMap()
      }
    }.also {
      val countOfBooks = scannedSeries.values.sumBy { it.size }
      logger.info { "Scanned ${scannedSeries.size} series and $countOfBooks books in $it" }
    }

    return scannedSeries
  }
}

fun Path.getUpdatedTime(): LocalDateTime =
  Files.readAttributes(this, BasicFileAttributes::class.java).let {
    maxOf(it.creationTime(), it.lastModifiedTime()).toLocalDateTime()
  }

fun FileTime.toLocalDateTime(): LocalDateTime =
  LocalDateTime.ofInstant(this.toInstant(), ZoneId.systemDefault())
