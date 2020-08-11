package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookPage
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaNotReadyException
import org.gotson.komga.domain.model.MediaUnsupportedException
import org.gotson.komga.domain.model.ThumbnailBook
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.infrastructure.image.ImageConverter
import org.gotson.komga.infrastructure.mediacontainer.ContentDetector
import org.gotson.komga.infrastructure.mediacontainer.MediaContainerExtractor
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class BookAnalyzer(
  private val contentDetector: ContentDetector,
  extractors: List<MediaContainerExtractor>,
  private val imageConverter: ImageConverter,
  private val mediaRepository: MediaRepository
) {

  val supportedMediaTypes = extractors
    .flatMap { e -> e.mediaTypes().map { it to e } }
    .toMap()

  private val thumbnailSize = 300
  private val thumbnailFormat = "jpeg"

  fun analyze(book: Book): Media {
    logger.info { "Trying to analyze book: $book" }

    val mediaType = contentDetector.detectMediaType(book.path())
    logger.info { "Detected media type: $mediaType" }
    if (!supportedMediaTypes.containsKey(mediaType))
      return Media(mediaType = mediaType, status = Media.Status.UNSUPPORTED, comment = "Media type $mediaType is not supported")

    val entries = try {
      supportedMediaTypes.getValue(mediaType).getEntries(book.path())
    } catch (ex: MediaUnsupportedException) {
      return Media(mediaType = mediaType, status = Media.Status.UNSUPPORTED, comment = ex.message)
    } catch (ex: Exception) {
      logger.error(ex) { "Error while analyzing book: $book" }
      return Media(mediaType = mediaType, status = Media.Status.ERROR, comment = ex.message)
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
      ?.joinToString(prefix = "Some entries could not be analyzed: [", postfix = "]") { it }

    if (pages.isEmpty()) {
      logger.warn { "Book $book does not contain any pages" }
      return Media(mediaType = mediaType, status = Media.Status.ERROR, comment = "Book does not contain any pages")
    }
    logger.info { "Book has ${pages.size} pages" }

    val files = others.map { it.name }

    return Media(mediaType = mediaType, status = Media.Status.READY, pages = pages, files = files, comment = entriesErrorSummary)
  }

  @Throws(MediaNotReadyException::class)
  fun generateThumbnail(book: Book): ThumbnailBook {
    logger.info { "Generate thumbnail for book: $book" }

    val media = mediaRepository.findById(book.id)

    if (media.status != Media.Status.READY) {
      logger.warn { "Book media is not ready, cannot generate thumbnail. Book: $book" }
      throw MediaNotReadyException()
    }

    val thumbnail = try {
      supportedMediaTypes.getValue(media.mediaType!!).getEntryStream(book.path(), media.pages.first().fileName).let { cover ->
        imageConverter.resizeImage(cover, thumbnailFormat, thumbnailSize)
      }
    } catch (ex: Exception) {
      logger.warn(ex) { "Could not generate thumbnail for book: $book" }
      null
    }

    return ThumbnailBook(
      thumbnail = thumbnail,
      type = ThumbnailBook.Type.GENERATED,
      bookId = book.id
    )
  }

  @Throws(
    MediaNotReadyException::class,
    IndexOutOfBoundsException::class
  )
  fun getPageContent(book: Book, number: Int): ByteArray {
    logger.info { "Get page #$number for book: $book" }

    val media = mediaRepository.findById(book.id)

    if (media.status != Media.Status.READY) {
      logger.warn { "Book media is not ready, cannot get pages" }
      throw MediaNotReadyException()
    }

    if (number > media.pages.size || number <= 0) {
      logger.error { "Page number #$number is out of bounds. Book has ${media.pages.size} pages" }
      throw IndexOutOfBoundsException("Page $number does not exist")
    }

    return supportedMediaTypes.getValue(media.mediaType!!).getEntryStream(book.path(), media.pages[number - 1].fileName)
  }

  @Throws(
    MediaNotReadyException::class
  )
  fun getFileContent(book: Book, fileName: String): ByteArray {
    logger.info { "Get file $fileName for book: $book" }

    val media = mediaRepository.findById(book.id)

    if (media.status != Media.Status.READY) {
      logger.warn { "Book media is not ready, cannot get files" }
      throw MediaNotReadyException()
    }

    return supportedMediaTypes.getValue(media.mediaType!!).getEntryStream(book.path(), fileName)
  }
}
