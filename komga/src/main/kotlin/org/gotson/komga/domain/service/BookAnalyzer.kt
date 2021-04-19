package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookPage
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaNotReadyException
import org.gotson.komga.domain.model.MediaUnsupportedException
import org.gotson.komga.domain.model.ThumbnailBook
import org.gotson.komga.infrastructure.image.ImageConverter
import org.gotson.komga.infrastructure.mediacontainer.ContentDetector
import org.gotson.komga.infrastructure.mediacontainer.MediaContainerExtractor
import org.springframework.stereotype.Service
import java.nio.file.AccessDeniedException

private val logger = KotlinLogging.logger {}

@Service
class BookAnalyzer(
  private val contentDetector: ContentDetector,
  extractors: List<MediaContainerExtractor>,
  private val imageConverter: ImageConverter
) {

  val supportedMediaTypes = extractors
    .flatMap { e -> e.mediaTypes().map { it to e } }
    .toMap()

  private val thumbnailSize = 300
  private val thumbnailFormat = "jpeg"

  fun analyze(book: Book): Media {
    logger.info { "Trying to analyze book: $book" }
    try {
      val mediaType = contentDetector.detectMediaType(book.path())
      logger.info { "Detected media type: $mediaType" }
      if (!supportedMediaTypes.containsKey(mediaType))
        return Media(mediaType = mediaType, status = Media.Status.UNSUPPORTED, comment = "ERR_1001", bookId = book.id)

      val entries = try {
        supportedMediaTypes.getValue(mediaType).getEntries(book.path())
      } catch (ex: MediaUnsupportedException) {
        return Media(mediaType = mediaType, status = Media.Status.UNSUPPORTED, comment = ex.code, bookId = book.id)
      } catch (ex: Exception) {
        logger.error(ex) { "Error while analyzing book: $book" }
        return Media(mediaType = mediaType, status = Media.Status.ERROR, comment = "ERR_1008", bookId = book.id)
      }

      val (pages, others) = entries
        .partition { entry ->
          entry.mediaType?.let { contentDetector.isImage(it) } ?: false
        }.let { (images, others) ->
          Pair(
            images.map { BookPage(it.name, it.mediaType!!, it.dimension) },
            others
          )
        }

      val entriesErrorSummary = others
        .filter { it.mediaType.isNullOrBlank() }
        .map { it.name }
        .ifEmpty { null }
        ?.joinToString(prefix = "ERR_1007 [", postfix = "]") { it }

      if (pages.isEmpty()) {
        logger.warn { "Book $book does not contain any pages" }
        return Media(mediaType = mediaType, status = Media.Status.ERROR, comment = "ERR_1006", bookId = book.id)
      }
      logger.info { "Book has ${pages.size} pages" }

      val files = others.map { it.name }

      return Media(mediaType = mediaType, status = Media.Status.READY, pages = pages, files = files, comment = entriesErrorSummary, bookId = book.id)
    } catch (ade: AccessDeniedException) {
      logger.error(ade) { "Error while analyzing book: $book" }
      return Media(status = Media.Status.ERROR, comment = "ERR_1000", bookId = book.id)
    } catch (ex: Exception) {
      logger.error(ex) { "Error while analyzing book: $book" }
      return Media(status = Media.Status.ERROR, comment = "ERR_1005", bookId = book.id)
    }
  }

  @Throws(MediaNotReadyException::class)
  fun generateThumbnail(book: BookWithMedia): ThumbnailBook {
    logger.info { "Generate thumbnail for book: $book" }

    if (book.media.status != Media.Status.READY) {
      logger.warn { "Book media is not ready, cannot generate thumbnail. Book: $book" }
      throw MediaNotReadyException()
    }

    val thumbnail = try {
      supportedMediaTypes.getValue(book.media.mediaType!!).getEntryStream(book.book.path(), book.media.pages.first().fileName).let { cover ->
        imageConverter.resizeImage(cover, thumbnailFormat, thumbnailSize)
      }
    } catch (ex: Exception) {
      logger.warn(ex) { "Could not generate thumbnail for book: $book" }
      null
    }

    return ThumbnailBook(
      thumbnail = thumbnail,
      type = ThumbnailBook.Type.GENERATED,
      bookId = book.book.id
    )
  }

  @Throws(
    MediaNotReadyException::class,
    IndexOutOfBoundsException::class
  )
  fun getPageContent(book: BookWithMedia, number: Int): ByteArray {
    logger.info { "Get page #$number for book: $book" }

    if (book.media.status != Media.Status.READY) {
      logger.warn { "Book media is not ready, cannot get pages" }
      throw MediaNotReadyException()
    }

    if (number > book.media.pages.size || number <= 0) {
      logger.error { "Page number #$number is out of bounds. Book has ${book.media.pages.size} pages" }
      throw IndexOutOfBoundsException("Page $number does not exist")
    }

    return supportedMediaTypes.getValue(book.media.mediaType!!).getEntryStream(book.book.path(), book.media.pages[number - 1].fileName)
  }

  @Throws(
    MediaNotReadyException::class
  )
  fun getFileContent(book: BookWithMedia, fileName: String): ByteArray {
    logger.info { "Get file $fileName for book: $book" }

    if (book.media.status != Media.Status.READY) {
      logger.warn { "Book media is not ready, cannot get files" }
      throw MediaNotReadyException()
    }

    return supportedMediaTypes.getValue(book.media.mediaType!!).getEntryStream(book.book.path(), fileName)
  }
}
