package org.gotson.komga.infrastructure.metadata.localmediaassets

import mu.KotlinLogging
import org.apache.commons.io.FilenameUtils
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.ThumbnailBook
import org.gotson.komga.infrastructure.mediacontainer.ContentDetector
import org.springframework.stereotype.Service
import java.nio.file.Files
import kotlin.streams.asSequence

private val logger = KotlinLogging.logger {}

@Service
class LocalMediaAssetsProvider(
  private val contentDetector: ContentDetector
) {

  val supportedExtensions = listOf("png", "jpeg", "jpg", "tbn")

  fun getBookThumbnails(book: Book): List<ThumbnailBook> {
    logger.info { "Looking for local thumbnails for book: $book" }
    val bookPath = book.path()
    val baseName = FilenameUtils.getBaseName(bookPath.toString())

    val regex = "${Regex.escape(baseName)}(-\\d+)?".toRegex(RegexOption.IGNORE_CASE)

    return Files.list(bookPath.parent).use { dirStream ->
      dirStream.asSequence()
        .filter { Files.isRegularFile(it) }
        .filter { regex.matches(FilenameUtils.getBaseName(it.toString())) }
        .filter { supportedExtensions.contains(FilenameUtils.getExtension(it.fileName.toString()).toLowerCase()) }
        .filter { contentDetector.isImage(contentDetector.detectMediaType(it)) }
        .mapIndexed { index, path ->
          logger.info { "Found file: $path" }
          ThumbnailBook(
            url = path.toUri().toURL(),
            type = ThumbnailBook.Type.SIDECAR,
            bookId = book.id,
            selected = index == 0
          )
        }.sortedBy { it.url.toString() }
        .toList()
    }
  }

}
