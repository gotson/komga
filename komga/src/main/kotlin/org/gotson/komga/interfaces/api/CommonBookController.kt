package org.gotson.komga.interfaces.api

import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.EntryNotFoundException
import org.gotson.komga.domain.model.ImageConversionException
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaNotReadyException
import org.gotson.komga.domain.model.MediaProfile
import org.gotson.komga.domain.model.MediaUnsupportedException
import org.gotson.komga.domain.model.ROLE_FILE_DOWNLOAD
import org.gotson.komga.domain.model.ROLE_PAGE_STREAMING
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.domain.service.BookAnalyzer
import org.gotson.komga.domain.service.BookLifecycle
import org.gotson.komga.infrastructure.image.ImageType
import org.gotson.komga.infrastructure.mediacontainer.ContentDetector
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.infrastructure.web.getMediaTypeOrDefault
import org.gotson.komga.interfaces.api.persistence.BookDtoRepository
import org.springframework.core.io.FileSystemResource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.util.MimeTypeUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.io.FileNotFoundException
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.NoSuchFileException
import kotlin.io.path.name

private val logger = KotlinLogging.logger {}
private val FONT_EXTENSIONS = listOf("otf", "woff", "woff2", "eot", "ttf", "svg")

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class CommonBookController(
  private val mediaRepository: MediaRepository,
  private val bookRepository: BookRepository,
  private val bookDtoRepository: BookDtoRepository,
  private val seriesMetadataRepository: SeriesMetadataRepository,
  private val bookLifecycle: BookLifecycle,
  private val bookAnalyzer: BookAnalyzer,
  private val contentRestrictionChecker: ContentRestrictionChecker,
  private val contentDetector: ContentDetector,
) {
  fun getWebPubManifestInternal(
    principal: KomgaPrincipal,
    bookId: String,
    webPubGenerator: WebPubGenerator,
  ) =
    mediaRepository.findByIdOrNull(bookId)?.let { media ->
      when (org.gotson.komga.domain.model.MediaType.fromMediaType(media.mediaType)?.profile) {
        MediaProfile.DIVINA -> getWebPubManifestDivinaInternal(principal, bookId, webPubGenerator)
        MediaProfile.PDF -> getWebPubManifestPdfInternal(principal, bookId, webPubGenerator)
        MediaProfile.EPUB -> getWebPubManifestEpubInternal(principal, bookId, webPubGenerator)
        null -> throw ResponseStatusException(HttpStatus.NOT_FOUND, "Book analysis failed")
      }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  fun getWebPubManifestEpubInternal(
    principal: KomgaPrincipal,
    bookId: String,
    webPubGenerator: WebPubGenerator,
  ) =
    bookDtoRepository.findByIdOrNull(bookId, principal.user.id)?.let { bookDto ->
      if (bookDto.media.mediaProfile != MediaProfile.EPUB.name) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Book media type '${bookDto.media.mediaType}' not compatible with requested profile")
      contentRestrictionChecker.checkContentRestriction(principal.user, bookDto)
      webPubGenerator.toManifestEpub(
        bookDto,
        mediaRepository.findById(bookId),
        seriesMetadataRepository.findById(bookDto.seriesId),
      )
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  fun getWebPubManifestPdfInternal(
    principal: KomgaPrincipal,
    bookId: String,
    webPubGenerator: WebPubGenerator,
  ) =
    bookDtoRepository.findByIdOrNull(bookId, principal.user.id)?.let { bookDto ->
      if (bookDto.media.mediaProfile != MediaProfile.PDF.name) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Book media type '${bookDto.media.mediaType}' not compatible with requested profile")
      contentRestrictionChecker.checkContentRestriction(principal.user, bookDto)
      webPubGenerator.toManifestPdf(
        bookDto,
        mediaRepository.findById(bookDto.id),
        seriesMetadataRepository.findById(bookDto.seriesId),
      )
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  fun getWebPubManifestDivinaInternal(
    principal: KomgaPrincipal,
    bookId: String,
    webPubGenerator: WebPubGenerator,
  ) =
    bookDtoRepository.findByIdOrNull(bookId, principal.user.id)?.let { bookDto ->
      contentRestrictionChecker.checkContentRestriction(principal.user, bookDto)
      webPubGenerator.toManifestDivina(
        bookDto,
        mediaRepository.findById(bookDto.id),
        seriesMetadataRepository.findById(bookDto.seriesId),
      )
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  fun getBookPageInternal(
    bookId: String,
    pageNumber: Int,
    convertTo: String?,
    request: ServletWebRequest,
    principal: KomgaPrincipal,
    acceptHeaders: MutableList<MediaType>?,
  ) =
    bookRepository.findByIdOrNull((bookId))?.let { book ->
      val media = mediaRepository.findById(bookId)
      if (request.checkNotModified(getBookLastModified(media))) {
        return@let ResponseEntity
          .status(HttpStatus.NOT_MODIFIED)
          .setNotModified(media)
          .body(ByteArray(0))
      }

      contentRestrictionChecker.checkContentRestriction(principal.user, book)

      if (media.profile == MediaProfile.PDF && acceptHeaders != null && acceptHeaders.any { it.isCompatibleWith(MediaType.APPLICATION_PDF) }) {
        // keep only pdf and image
        acceptHeaders.removeIf { !it.isCompatibleWith(MediaType.APPLICATION_PDF) && !it.isCompatibleWith(MediaType("image")) }
        MimeTypeUtils.sortBySpecificity(acceptHeaders)
        if (acceptHeaders.first().isCompatibleWith(MediaType.APPLICATION_PDF))
          return getBookPageRawInternal(book, media, pageNumber)
      }

      try {
        val convertFormat =
          when (convertTo?.lowercase()) {
            "jpeg" -> ImageType.JPEG
            "png" -> ImageType.PNG
            "", null -> null
            else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid conversion format: $convertTo")
          }

        val pageContent = bookLifecycle.getBookPage(book, pageNumber, convertFormat)

        ResponseEntity.ok()
          .headers(
            HttpHeaders().apply {
              val extension = contentDetector.mediaTypeToExtension(pageContent.mediaType) ?: "jpeg"
              val imageFileName = "${book.name}-$pageNumber$extension"
              contentDisposition =
                ContentDisposition.builder("inline")
                  .filename(imageFileName, StandardCharsets.UTF_8)
                  .build()
            },
          )
          .contentType(getMediaTypeOrDefault(pageContent.mediaType))
          .setNotModified(media)
          .body(pageContent.bytes)
      } catch (ex: IndexOutOfBoundsException) {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Page number does not exist")
      } catch (ex: ImageConversionException) {
        throw ResponseStatusException(HttpStatus.NOT_FOUND, ex.message)
      } catch (ex: MediaNotReadyException) {
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "Book analysis failed")
      } catch (ex: NoSuchFileException) {
        logger.warn(ex) { "File not found: $book" }
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "File not found, it may have moved")
      }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @GetMapping(
    value = [
      "api/v1/books/{bookId}/pages/{pageNumber}/raw",
      "opds/v2/books/{bookId}/pages/{pageNumber}/raw",
    ],
    produces = [MediaType.ALL_VALUE],
  )
  @PreAuthorize("hasRole('$ROLE_PAGE_STREAMING')")
  fun getBookPageRaw(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    request: ServletWebRequest,
    @PathVariable bookId: String,
    @PathVariable pageNumber: Int,
  ): ResponseEntity<ByteArray> =
    bookRepository.findByIdOrNull((bookId))?.let { book ->
      val media = mediaRepository.findById(bookId)
      if (request.checkNotModified(getBookLastModified(media))) {
        return@let ResponseEntity
          .status(HttpStatus.NOT_MODIFIED)
          .setNotModified(media)
          .body(ByteArray(0))
      }

      contentRestrictionChecker.checkContentRestriction(principal.user, book)

      getBookPageRawInternal(book, media, pageNumber)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  fun getBookPageRawInternal(
    book: Book,
    media: Media,
    pageNumber: Int,
  ): ResponseEntity<ByteArray> =
    try {
      val pageContent = bookAnalyzer.getPageContentRaw(BookWithMedia(book, media), pageNumber)

      ResponseEntity.ok()
        .headers(
          HttpHeaders().apply {
            val extension = contentDetector.mediaTypeToExtension(pageContent.mediaType) ?: ""
            val pageFileName = "${book.name}-$pageNumber$extension"
            contentDisposition =
              ContentDisposition.builder("inline")
                .filename(pageFileName, StandardCharsets.UTF_8)
                .build()
          },
        )
        .contentType(getMediaTypeOrDefault(pageContent.mediaType))
        .setNotModified(media)
        .body(pageContent.bytes)
    } catch (ex: IndexOutOfBoundsException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Page number does not exist")
    } catch (ex: MediaUnsupportedException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, ex.message)
    } catch (ex: MediaNotReadyException) {
      throw ResponseStatusException(HttpStatus.NOT_FOUND, "Book analysis failed")
    } catch (ex: NoSuchFileException) {
      logger.warn(ex) { "File not found: $book" }
      throw ResponseStatusException(HttpStatus.NOT_FOUND, "File not found, it may have moved")
    }

  @GetMapping(
    value = [
      "api/v1/books/{bookId}/resource/{*resource}",
      "opds/v2/books/{bookId}/resource/{*resource}",
    ],
    produces = ["*/*"],
  )
  fun getBookResource(
    request: HttpServletRequest,
    @AuthenticationPrincipal principal: KomgaPrincipal?,
    @PathVariable bookId: String,
    @PathVariable resource: String,
  ): ResponseEntity<ByteArray> {
    val resourceName = resource.removePrefix("/")
    val isFont = FONT_EXTENSIONS.contains(FilenameUtils.getExtension(resourceName).lowercase())

    if (!isFont && principal == null) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)

    val book = bookRepository.findByIdOrNull(bookId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    val media = mediaRepository.findById(book.id)

    if (ServletWebRequest(request).checkNotModified(getBookLastModified(media))) {
      return ResponseEntity
        .status(HttpStatus.NOT_MODIFIED)
        .setNotModified(media)
        .body(ByteArray(0))
    }

    if (media.profile != MediaProfile.EPUB) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Book media type '${media.mediaType}' not compatible with requested profile")
    if (!isFont) contentRestrictionChecker.checkContentRestriction(principal!!.user, book)

    val res = media.files.firstOrNull { it.fileName == resourceName } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    val bytes =
      try {
        bookAnalyzer.getFileContent(BookWithMedia(book, media), resourceName)
      } catch (e: EntryNotFoundException) {
        throw ResponseStatusException(HttpStatus.NOT_FOUND)
      }

    return ResponseEntity.ok()
      .headers(
        HttpHeaders().apply {
          contentDisposition =
            ContentDisposition.builder("inline")
              .filename(FilenameUtils.getName(resourceName), StandardCharsets.UTF_8)
              .build()
        },
      )
      .contentType(getMediaTypeOrDefault(res.mediaType))
      .setNotModified(media)
      .body(bytes)
  }

  @Operation(description = "Download the book file.")
  @GetMapping(
    value = [
      "api/v1/books/{bookId}/file",
      "api/v1/books/{bookId}/file/*",
      "opds/v1.2/books/{bookId}/file/*",
      "opds/v2/books/{bookId}/file",
      "opds/v2/books/{bookId}/file/*",
    ],
    produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE],
  )
  @PreAuthorize("hasRole('$ROLE_FILE_DOWNLOAD')")
  fun getBookFile(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String,
  ): ResponseEntity<StreamingResponseBody> =
    bookRepository.findByIdOrNull(bookId)?.let { book ->
      contentRestrictionChecker.checkContentRestriction(principal.user, book)
      try {
        val media = mediaRepository.findById(book.id)
        with(FileSystemResource(book.path)) {
          if (!exists()) throw FileNotFoundException(path)
          val stream =
            StreamingResponseBody { os: OutputStream ->
              this.inputStream.use {
                IOUtils.copyLarge(it, os, ByteArray(8192))
                os.close()
              }
            }
          ResponseEntity.ok()
            .headers(
              HttpHeaders().apply {
                contentDisposition =
                  ContentDisposition.builder("attachment")
                    .filename(book.path.name, StandardCharsets.UTF_8)
                    .build()
              },
            )
            .contentType(getMediaTypeOrDefault(media.mediaType))
            .contentLength(this.contentLength())
            .body(stream)
        }
      } catch (ex: FileNotFoundException) {
        logger.warn(ex) { "File not found: $book" }
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "File not found, it may have moved")
      }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
}
