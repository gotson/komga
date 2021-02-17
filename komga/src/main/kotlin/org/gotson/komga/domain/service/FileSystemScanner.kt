package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.apache.commons.io.FilenameUtils
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.DirectoryNotFoundException
import org.gotson.komga.domain.model.Series
import org.gotson.komga.infrastructure.configuration.KomgaProperties
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
import kotlin.streams.asSequence
import kotlin.time.measureTime

private val logger = KotlinLogging.logger {}

@Service
class FileSystemScanner(
  private val komgaProperties: KomgaProperties
) {

  val supportedExtensions = listOf("cbz", "zip", "cbr", "rar", "pdf", "epub")

  fun scanRootFolder(root: Path, forceDirectoryModifiedTime: Boolean = false): Map<Series, List<Book>> {
    logger.info { "Scanning folder: $root" }
    logger.info { "Supported extensions: $supportedExtensions" }
    logger.info { "Excluded patterns: ${komgaProperties.librariesScanDirectoryExclusions}" }
    logger.info { "Force directory modified time: $forceDirectoryModifiedTime" }

    if (!(Files.isDirectory(root) && Files.isReadable(root)))
      throw DirectoryNotFoundException("Library root is not accessible: $root")

    lateinit var scannedSeries: Map<Series, List<Book>>

    measureTime {
      val dirs = mutableListOf<Path>()

      Files.walkFileTree(
        root, setOf(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
        object : FileVisitor<Path> {
          override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes?): FileVisitResult {
            logger.trace { "preVisit: $dir" }
            if (Files.isHidden(dir) || komgaProperties.librariesScanDirectoryExclusions.any { exclude ->
              dir.toString().contains(exclude, true)
            }
            ) return FileVisitResult.SKIP_SUBTREE

            dirs.add(dir)
            return FileVisitResult.CONTINUE
          }

          override fun visitFile(file: Path?, attrs: BasicFileAttributes?): FileVisitResult = FileVisitResult.CONTINUE

          override fun visitFileFailed(file: Path?, exc: IOException?): FileVisitResult {
            logger.warn { "Could not access: $file" }
            return FileVisitResult.SKIP_SUBTREE
          }

          override fun postVisitDirectory(dir: Path?, exc: IOException?): FileVisitResult = FileVisitResult.CONTINUE
        }
      )

      logger.debug { "Found directories: $dirs" }

      scannedSeries = dirs
        .mapNotNull { dir ->
          logger.debug { "Processing directory: $dir" }
          val books = Files.list(dir).use { dirStream ->
            dirStream.asSequence()
              .onEach { logger.trace { "GetBooks file: $it" } }
              .filterNot { Files.isHidden(it) }
              .filter { Files.isReadable(it) }
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
            if (forceDirectoryModifiedTime)
              maxOf(dir.getUpdatedTime(), books.map { it.fileLastModified }.maxOrNull()!!)
            else dir.getUpdatedTime()
          ) to books
        }.toMap()
    }.also {
      val countOfBooks = scannedSeries.values.sumBy { it.size }
      logger.info { "Scanned ${scannedSeries.size} series and $countOfBooks books in $it" }
    }

    return scannedSeries
  }
}

fun Path.getUpdatedTime(): LocalDateTime =
  Files.readAttributes(this, BasicFileAttributes::class.java).let { b ->
    maxOf(b.creationTime(), b.lastModifiedTime()).toLocalDateTime()
      .also { logger.trace { "Get updated time for file $this. Creation time: ${b.creationTime()}, Last Modified Time: ${b.lastModifiedTime()}. Choosing the max (Local Time): $it" } }
  }

fun FileTime.toLocalDateTime(): LocalDateTime =
  LocalDateTime.ofInstant(this.toInstant(), ZoneId.systemDefault())
