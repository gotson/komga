package org.gotson.komga.domain.service

import mu.KotlinLogging
import net.coobird.thumbnailator.Thumbnails
import net.greypanther.natsort.CaseInsensitiveSimpleNaturalComparator
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaNotReadyException
import org.gotson.komga.domain.model.UnsupportedMediaTypeException
import org.gotson.komga.infrastructure.archive.ContentDetector
import org.gotson.komga.infrastructure.archive.PdfExtractor
import org.gotson.komga.infrastructure.archive.RarExtractor
import org.gotson.komga.infrastructure.archive.ZipExtractor
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.util.*

private val logger = KotlinLogging.logger {}

@Service
class BookAnalyzer(
    private val contentDetector: ContentDetector,
    private val zipExtractor: ZipExtractor,
    private val rarExtractor: RarExtractor,
    private val pdfExtractor: PdfExtractor
) {

  val supportedMediaTypes = mapOf(
      "application/zip" to zipExtractor,
      "application/x-rar-compressed" to rarExtractor,
      "application/pdf" to pdfExtractor
  )

  private val natSortComparator: Comparator<String> = CaseInsensitiveSimpleNaturalComparator.getInstance()

  private val thumbnailSize = 300
  private val thumbnailFormat = "jpeg"

  @Throws(UnsupportedMediaTypeException::class)
  fun analyze(book: Book): Media {
    logger.info { "Trying to analyze book: $book" }

    val mediaType = contentDetector.detectMediaType(book.path())
    logger.info { "Detected media type: $mediaType" }
    if (!supportedMediaTypes.keys.contains(mediaType))
      throw UnsupportedMediaTypeException("Unsupported mime type: $mediaType. File: $book", mediaType)

    val pages = supportedMediaTypes.getValue(mediaType).getPagesList(book.path())
        .sortedWith(compareBy(natSortComparator) { it.fileName })
    logger.info { "Book has ${pages.size} pages" }

    logger.info { "Trying to generate cover for book: $book" }
    val thumbnail = generateThumbnail(book, mediaType, pages.first().fileName)

    return Media(mediaType = mediaType, status = Media.Status.READY, pages = pages, thumbnail = thumbnail)
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
        ByteArrayOutputStream().use {
          supportedMediaTypes.getValue(mediaType).getPageStream(book.path(), entry).let { cover ->
            Thumbnails.of(cover.inputStream())
                .size(thumbnailSize, thumbnailSize)
                .outputFormat(thumbnailFormat)
                .toOutputStream(it)
            it.toByteArray()
          }
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

    return supportedMediaTypes.getValue(book.media.mediaType!!).getPageStream(book.path(), book.media.pages[number - 1].fileName)
  }
}
