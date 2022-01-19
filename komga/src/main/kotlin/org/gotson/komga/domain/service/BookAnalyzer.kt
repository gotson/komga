package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookPage
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaNotReadyException
import org.gotson.komga.domain.model.MediaUnsupportedException
import org.gotson.komga.domain.model.ThumbnailBook
import org.gotson.komga.infrastructure.hash.Hasher
import org.gotson.komga.infrastructure.image.ImageConverter
import org.gotson.komga.infrastructure.image.ImageType
import org.gotson.komga.infrastructure.mediacontainer.ContentDetector
import org.gotson.komga.infrastructure.mediacontainer.MediaContainerExtractor
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.nio.file.AccessDeniedException
import java.nio.file.NoSuchFileException
import javax.imageio.ImageIO

private val logger = KotlinLogging.logger {}

@Service
class BookAnalyzer(
  private val contentDetector: ContentDetector,
  extractors: List<MediaContainerExtractor>,
  private val imageConverter: ImageConverter,
  private val hasher: Hasher,
  @Value("#{@komgaProperties.pageHashing}") private val pageHashing: Int,
) {

  val supportedMediaTypes = extractors
    .flatMap { e -> e.mediaTypes().map { it to e } }
    .toMap()

  private val thumbnailSize = 300
  private val thumbnailFormat = "jpeg"

  fun analyze(book: Book, analyzeDimensions: Boolean): Media {
    logger.info { "Trying to analyze book: $book" }
    try {
      val mediaType = contentDetector.detectMediaType(book.path)
      logger.info { "Detected media type: $mediaType" }
      if (!supportedMediaTypes.containsKey(mediaType))
        return Media(mediaType = mediaType, status = Media.Status.UNSUPPORTED, comment = "ERR_1001", bookId = book.id)

      val entries = try {
        supportedMediaTypes.getValue(mediaType).getEntries(book.path, analyzeDimensions)
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
            images.map { BookPage(fileName = it.name, mediaType = it.mediaType!!, dimension = it.dimension, fileSize = it.fileSize) },
            others,
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
    } catch (ex: NoSuchFileException) {
      logger.error(ex) { "Error while analyzing book: $book" }
      return Media(status = Media.Status.ERROR, comment = "ERR_1018", bookId = book.id)
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
      supportedMediaTypes.getValue(book.media.mediaType!!).getEntryStream(book.book.path, book.media.pages.first().fileName).let { cover ->
        imageConverter.resizeImage(cover, thumbnailFormat, thumbnailSize)
      }
    } catch (ex: Exception) {
      logger.warn(ex) { "Could not generate thumbnail for book: $book" }
      null
    }

    return ThumbnailBook(
      thumbnail = thumbnail,
      type = ThumbnailBook.Type.GENERATED,
      bookId = book.book.id,
    )
  }

  @Throws(
    MediaNotReadyException::class,
    IndexOutOfBoundsException::class,
  )
  fun getPageContent(book: BookWithMedia, number: Int): ByteArray {
    logger.debug { "Get page #$number for book: $book" }

    if (book.media.status != Media.Status.READY) {
      logger.warn { "Book media is not ready, cannot get pages" }
      throw MediaNotReadyException()
    }

    if (number > book.media.pages.size || number <= 0) {
      logger.error { "Page number #$number is out of bounds. Book has ${book.media.pages.size} pages" }
      throw IndexOutOfBoundsException("Page $number does not exist")
    }

    return supportedMediaTypes.getValue(book.media.mediaType!!).getEntryStream(book.book.path, book.media.pages[number - 1].fileName)
  }

  @Throws(
    MediaNotReadyException::class,
  )
  fun getFileContent(book: BookWithMedia, fileName: String): ByteArray {
    logger.debug { "Get file $fileName for book: $book" }

    if (book.media.status != Media.Status.READY) {
      logger.warn { "Book media is not ready, cannot get files" }
      throw MediaNotReadyException()
    }

    return supportedMediaTypes.getValue(book.media.mediaType!!).getEntryStream(book.book.path, fileName)
  }

  /**
   * Will hash the first and last pages of the given book.
   * The number of pages hashed from start/end is configurable.
   *
   * See [org.gotson.komga.infrastructure.configuration.KomgaProperties.pageHashing]
   */
  fun hashPages(book: BookWithMedia): Media {
    val hashedPages = book.media.pages.mapIndexed { index, bookPage ->
      if (bookPage.fileHash.isBlank() && (index < pageHashing || index >= (book.media.pages.size - pageHashing))) {
        val content = getPageContent(book, index + 1)
        val hash = hashPage(bookPage, content)
        bookPage.copy(fileHash = hash)
      } else bookPage
    }

    return book.media.copy(pages = hashedPages)
  }

  /**
   * Hash a single page, using the file content for hashing.
   *
   * For JPEG, the image is read/written to remove the metadata.
   */
  fun hashPage(page: BookPage, content: ByteArray): String {
    val bytes =
      if (page.mediaType == ImageType.JPEG.mediaType) {
        // JPEG could contain different EXIF data, reading and writing back the image will get rid of it
        ByteArrayOutputStream().use { buffer ->
          ImageIO.write(ImageIO.read(content.inputStream()), ImageType.JPEG.imageIOFormat, buffer)
          buffer.toByteArray()
        }
      } else content

    return hasher.computeHash(bytes.inputStream())
  }
}
