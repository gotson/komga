package org.gotson.komga.domain.service

import mu.KotlinLogging
import net.greypanther.natsort.CaseInsensitiveSimpleNaturalComparator
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookPage
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaNotReadyException
import org.gotson.komga.infrastructure.image.ImageConverter
import org.gotson.komga.infrastructure.mediacontainer.ContentDetector
import org.gotson.komga.infrastructure.mediacontainer.MediaContainerExtractor
import org.springframework.stereotype.Service
import java.util.*

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

  private val natSortComparator: Comparator<String> = CaseInsensitiveSimpleNaturalComparator.getInstance()

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
    } catch (ex: Exception) {
      logger.error(ex) { "Error while analyzing book: $book" }
      return Media(mediaType = mediaType, status = Media.Status.ERROR, comment = ex.message)
    }

    val (pages, others) = entries
      .partition { entry ->
        entry.mediaType?.let { contentDetector.isImage(it) } ?: false
      }.let { (images, others) ->
        Pair(
          images
            .map { BookPage(it.name, it.mediaType!!) }
            .sortedWith(compareBy(natSortComparator) { it.fileName }),
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

    logger.info { "Trying to generate cover for book: $book" }
    val thumbnail = generateThumbnail(book, mediaType, pages.first().fileName)

    val files = others.map { it.name }

    return Media(mediaType = mediaType, status = Media.Status.READY, pages = pages, files = files, thumbnail = thumbnail, comment = entriesErrorSummary)
  }

  @Throws(MediaNotReadyException::class)
  fun regenerateThumbnail(book: Book): Media {
    logger.info { "Regenerate thumbnail for book: $book" }

    if (book.media.status != Media.Status.READY) {
      logger.warn { "Book media is not ready, cannot generate thumbnail. Book: $book" }
      throw MediaNotReadyException()
    }

    val thumbnail = generateThumbnail(book, book.media.mediaType!!, book.media.pages.first().fileName)

    return Media(
      mediaType = book.media.mediaType,
      status = Media.Status.READY,
      pages = book.media.pages,
      thumbnail = thumbnail
    )
  }

  private fun generateThumbnail(book: Book, mediaType: String, entry: String): ByteArray? =
    try {
      supportedMediaTypes.getValue(mediaType).getEntryStream(book.path(), entry).let { cover ->
        imageConverter.resizeImage(cover, thumbnailFormat, thumbnailSize)
      }
    } catch (ex: Exception) {
      logger.warn(ex) { "Could not generate thumbnail for book: $book" }
      null
    }

  @Throws(
    MediaNotReadyException::class,
    IndexOutOfBoundsException::class
  )
  fun getPageContent(book: Book, number: Int): ByteArray {
    logger.info { "Get page #$number for book: $book" }

    if (book.media.status != Media.Status.READY) {
      logger.warn { "Book media is not ready, cannot get pages" }
      throw MediaNotReadyException()
    }

    if (number > book.media.pages.size || number <= 0) {
      logger.error { "Page number #$number is out of bounds. Book has ${book.media.pages.size} pages" }
      throw IndexOutOfBoundsException("Page $number does not exist")
    }

    return supportedMediaTypes.getValue(book.media.mediaType!!).getEntryStream(book.path(), book.media.pages[number - 1].fileName)
  }

  @Throws(
    MediaNotReadyException::class
  )
  fun getFileContent(book: Book, fileName: String): ByteArray {
    logger.info { "Get file $fileName for book: $book" }

    if (book.media.status != Media.Status.READY) {
      logger.warn { "Book media is not ready, cannot get files" }
      throw MediaNotReadyException()
    }

    return supportedMediaTypes.getValue(book.media.mediaType!!).getEntryStream(book.path(), fileName)
  }
}
