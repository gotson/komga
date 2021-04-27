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
import kotlin.io.path.exists
import kotlin.io.path.readAttributes
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
      throw DirectoryNotFoundException("Folder is not accessible: $root", "ERR_1016")

    val scannedSeries = mutableMapOf<Series, List<Book>>()

    measureTime {
      val pathToSeries = mutableMapOf<Path, Series>()
      val pathToBooks = mutableMapOf<Path, MutableList<Book>>()

      Files.walkFileTree(
        root, setOf(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
        object : FileVisitor<Path> {
          override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
            logger.trace { "preVisit: $dir" }
            if (dir.fileName.toString().startsWith(".") ||
              komgaProperties.librariesScanDirectoryExclusions.any { exclude ->
                dir.toString().contains(exclude, true)
              }
            ) return FileVisitResult.SKIP_SUBTREE

            pathToSeries[dir] = Series(
              name = dir.fileName.toString(),
              url = dir.toUri().toURL(),
              fileLastModified = attrs.getUpdatedTime()
            )

            return FileVisitResult.CONTINUE
          }

          override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
            logger.trace { "visitFile: $file" }
            if (attrs.isRegularFile &&
              supportedExtensions.contains(FilenameUtils.getExtension(file.fileName.toString()).toLowerCase()) &&
              !file.fileName.toString().startsWith(".")
            ) {
              val book = pathToBook(file, attrs)
              file.parent.let { key ->
                if (pathToBooks.containsKey(key)) pathToBooks[key]!!.add(book)
                else pathToBooks[key] = mutableListOf(book)
              }
            }

            return FileVisitResult.CONTINUE
          }

          override fun visitFileFailed(file: Path?, exc: IOException?): FileVisitResult {
            logger.warn { "Could not access: $file" }
            return FileVisitResult.SKIP_SUBTREE
          }

          override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
            logger.trace { "postVisit: $dir" }
            val books = pathToBooks[dir]
            val series = pathToSeries[dir]
            if (!books.isNullOrEmpty() && series !== null) {
              if (forceDirectoryModifiedTime)
                scannedSeries[
                  series.copy(
                    fileLastModified = maxOf(
                      series.fileLastModified,
                      books.maxOf { it.fileLastModified }
                    )
                  )
                ] = books
              else
                scannedSeries[series] = books
            }

            return FileVisitResult.CONTINUE
          }
        }
      )
    }.also {
      val countOfBooks = scannedSeries.values.sumBy { it.size }
      logger.info { "Scanned ${scannedSeries.size} series and $countOfBooks books in $it" }
    }

    return scannedSeries
  }

  fun scanFile(path: Path): Book? {
    if (!path.exists()) return null

    return pathToBook(path, path.readAttributes())
  }

  private fun pathToBook(path: Path, attrs: BasicFileAttributes): Book =
    Book(
      name = FilenameUtils.getBaseName(path.fileName.toString()),
      url = path.toUri().toURL(),
      fileLastModified = attrs.getUpdatedTime(),
      fileSize = attrs.size()
    )
}

fun BasicFileAttributes.getUpdatedTime(): LocalDateTime =
  maxOf(creationTime(), lastModifiedTime()).toLocalDateTime()

fun FileTime.toLocalDateTime(): LocalDateTime =
  LocalDateTime.ofInstant(this.toInstant(), ZoneId.systemDefault())
