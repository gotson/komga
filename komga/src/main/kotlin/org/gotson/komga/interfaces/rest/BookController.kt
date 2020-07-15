package org.gotson.komga.interfaces.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import mu.KotlinLogging
import org.gotson.komga.application.tasks.TaskReceiver
import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.BookSearchWithReadProgress
import org.gotson.komga.domain.model.ImageConversionException
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaNotReadyException
import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.domain.model.ROLE_FILE_DOWNLOAD
import org.gotson.komga.domain.model.ROLE_PAGE_STREAMING
import org.gotson.komga.domain.model.ReadStatus
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.service.BookLifecycle
import org.gotson.komga.infrastructure.image.ImageType
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.infrastructure.swagger.PageableAsQueryParam
import org.gotson.komga.infrastructure.swagger.PageableWithoutSortAsQueryParam
import org.gotson.komga.interfaces.rest.dto.BookDto
import org.gotson.komga.interfaces.rest.dto.BookMetadataUpdateDto
import org.gotson.komga.interfaces.rest.dto.PageDto
import org.gotson.komga.interfaces.rest.dto.ReadProgressUpdateDto
import org.gotson.komga.interfaces.rest.dto.restrictUrl
import org.gotson.komga.interfaces.rest.persistence.BookDtoRepository
import org.springframework.core.io.FileSystemResource
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.CacheControl
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.WebRequest
import org.springframework.web.server.ResponseStatusException
import java.io.FileNotFoundException
import java.nio.file.NoSuchFileException
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit
import javax.validation.Valid

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class BookController(
  private val taskReceiver: TaskReceiver,
  private val bookLifecycle: BookLifecycle,
  private val bookRepository: BookRepository,
  private val bookMetadataRepository: BookMetadataRepository,
  private val mediaRepository: MediaRepository,
  private val bookDtoRepository: BookDtoRepository
) {

  @PageableAsQueryParam
  @GetMapping("api/v1/books")
  fun getAllBooks(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String?,
    @RequestParam(name = "library_id", required = false) libraryIds: List<String>?,
    @RequestParam(name = "media_status", required = false) mediaStatus: List<Media.Status>?,
    @RequestParam(name = "read_status", required = false) readStatus: List<ReadStatus>?,
    @Parameter(hidden = true) page: Pageable
  ): Page<BookDto> {
    val pageRequest = PageRequest.of(
      page.pageNumber,
      page.pageSize,
      if (page.sort.isSorted) Sort.by(page.sort.map { it.ignoreCase() }.toList())
      else Sort.by(Sort.Order.asc("metadata.title").ignoreCase())
    )

    val bookSearch = BookSearchWithReadProgress(
      libraryIds = principal.user.getAuthorizedLibraryIds(libraryIds),
      searchTerm = searchTerm,
      mediaStatus = mediaStatus,
      readStatus = readStatus
    )

    return bookDtoRepository.findAll(bookSearch, principal.user.id, pageRequest)
      .map { it.restrictUrl(!principal.user.roleAdmin) }
  }


  @Operation(description = "Return newly added or updated books.")
  @PageableWithoutSortAsQueryParam
  @GetMapping("api/v1/books/latest")
  fun getLatestBooks(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @Parameter(hidden = true) page: Pageable
  ): Page<BookDto> {
    val pageRequest = PageRequest.of(
      page.pageNumber,
      page.pageSize,
      Sort.by(Sort.Direction.DESC, "lastModifiedDate")
    )

    return bookDtoRepository.findAll(
      BookSearchWithReadProgress(
        libraryIds = principal.user.getAuthorizedLibraryIds(null)
      ),
      principal.user.id,
      pageRequest
    ).map { it.restrictUrl(!principal.user.roleAdmin) }
  }

  @Operation(description = "Return first unread book of series with at least one book read and no books in progress.")
  @PageableWithoutSortAsQueryParam
  @GetMapping("api/v1/books/ondeck")
  fun getBooksOnDeck(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @Parameter(hidden = true) page: Pageable
  ): Page<BookDto> {
    val libraryIds = if (principal.user.sharedAllLibraries) emptySet() else principal.user.sharedLibrariesIds

    return bookDtoRepository.findOnDeck(
      libraryIds,
      principal.user.id,
      page
    ).map { it.restrictUrl(!principal.user.roleAdmin) }
  }


  @GetMapping("api/v1/books/{bookId}")
  fun getOneBook(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String
  ): BookDto =
    bookDtoRepository.findByIdOrNull(bookId, principal.user.id)?.let {
      if (!principal.user.canAccessLibrary(it.libraryId)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
      it.restrictUrl(!principal.user.roleAdmin)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @GetMapping("api/v1/books/{bookId}/previous")
  fun getBookSiblingPrevious(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String
  ): BookDto {
    bookRepository.getLibraryId(bookId)?.let {
      if (!principal.user.canAccessLibrary(it)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    return bookDtoRepository.findPreviousInSeries(bookId, principal.user.id)
      ?.restrictUrl(!principal.user.roleAdmin)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @GetMapping("api/v1/books/{bookId}/next")
  fun getBookSiblingNext(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String
  ): BookDto {
    bookRepository.getLibraryId(bookId)?.let {
      if (!principal.user.canAccessLibrary(it)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    return bookDtoRepository.findNextInSeries(bookId, principal.user.id)
      ?.restrictUrl(!principal.user.roleAdmin)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }


  @ApiResponse(content = [Content(schema = Schema(type = "string", format = "binary"))])
  @GetMapping(value = [
    "api/v1/books/{bookId}/thumbnail",
    "opds/v1.2/books/{bookId}/thumbnail"
  ], produces = [MediaType.IMAGE_JPEG_VALUE])
  fun getBookThumbnail(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String
  ): ResponseEntity<ByteArray> {
    bookRepository.getLibraryId(bookId)?.let {
      if (!principal.user.canAccessLibrary(it)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    return mediaRepository.getThumbnail(bookId)?.let {
      ResponseEntity.ok()
        .setCachePrivate()
        .body(it)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @Operation(description = "Download the book file.")
  @GetMapping(value = [
    "api/v1/books/{bookId}/file",
    "api/v1/books/{bookId}/file/*",
    "opds/v1.2/books/{bookId}/file/*"
  ], produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
  @PreAuthorize("hasRole('$ROLE_FILE_DOWNLOAD')")
  fun getBookFile(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String
  ): ResponseEntity<FileSystemResource> =
    bookRepository.findByIdOrNull(bookId)?.let { book ->
      if (!principal.user.canAccessBook(book)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
      try {
        val media = mediaRepository.findById(book.id)
        with(FileSystemResource(book.path())) {
          if (!exists()) throw FileNotFoundException(path)
          ResponseEntity.ok()
            .headers(HttpHeaders().apply {
              contentDisposition = ContentDisposition.builder("attachment")
                .filename(book.fileName())
                .build()
            })
            .contentType(getMediaTypeOrDefault(media.mediaType))
            .body(this)
        }
      } catch (ex: FileNotFoundException) {
        logger.warn(ex) { "File not found: $book" }
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "File not found, it may have moved")
      }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)


  @GetMapping("api/v1/books/{bookId}/pages")
  fun getBookPages(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String
  ): List<PageDto> =
    bookRepository.findByIdOrNull(bookId)?.let { book ->
      if (!principal.user.canAccessBook(book)) throw ResponseStatusException(HttpStatus.FORBIDDEN)

      val media = mediaRepository.findById(book.id)
      when (media.status) {
        Media.Status.UNKNOWN -> throw ResponseStatusException(HttpStatus.NOT_FOUND, "Book has not been analyzed yet")
        Media.Status.OUTDATED -> throw ResponseStatusException(HttpStatus.NOT_FOUND, "Book is outdated and must be re-analyzed")
        Media.Status.ERROR -> throw ResponseStatusException(HttpStatus.NOT_FOUND, "Book analysis failed")
        Media.Status.UNSUPPORTED -> throw ResponseStatusException(HttpStatus.NOT_FOUND, "Book format is not supported")
        Media.Status.READY -> media.pages.mapIndexed { index, s -> PageDto(index + 1, s.fileName, s.mediaType) }
      }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @ApiResponse(content = [Content(
    mediaType = "image/*",
    schema = Schema(type = "string", format = "binary")
  )])
  @GetMapping(value = [
    "api/v1/books/{bookId}/pages/{pageNumber}",
    "opds/v1.2/books/{bookId}/pages/{pageNumber}"
  ])
  @PreAuthorize("hasRole('$ROLE_PAGE_STREAMING')")
  fun getBookPage(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    request: WebRequest,
    @PathVariable bookId: String,
    @PathVariable pageNumber: Int,
    @Parameter(description = "Convert the image to the provided format.", schema = Schema(allowableValues = ["jpeg", "png"]))
    @RequestParam(value = "convert", required = false) convertTo: String?,
    @Parameter(description = "If set to true, pages will start at index 0. If set to false, pages will start at index 1.")
    @RequestParam(value = "zero_based", defaultValue = "false") zeroBasedIndex: Boolean
  ): ResponseEntity<ByteArray> =
    bookRepository.findByIdOrNull((bookId))?.let { book ->
      val media = mediaRepository.findById(bookId)
      if (request.checkNotModified(getBookLastModified(media))) {
        return@let ResponseEntity
          .status(HttpStatus.NOT_MODIFIED)
          .setNotModified(media)
          .body(ByteArray(0))
      }
      if (!principal.user.canAccessBook(book)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
      try {
        val convertFormat = when (convertTo?.toLowerCase()) {
          "jpeg" -> ImageType.JPEG
          "png" -> ImageType.PNG
          "", null -> null
          else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid conversion format: $convertTo")
        }

        val pageNum = if (zeroBasedIndex) pageNumber + 1 else pageNumber

        val pageContent = bookLifecycle.getBookPage(book, pageNum, convertFormat)

        ResponseEntity.ok()
          .contentType(getMediaTypeOrDefault(pageContent.mediaType))
          .setNotModified(media)
          .body(pageContent.content)
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

  @ApiResponse(content = [Content(schema = Schema(type = "string", format = "binary"))])
  @GetMapping(
    value = ["api/v1/books/{bookId}/pages/{pageNumber}/thumbnail"],
    produces = [MediaType.IMAGE_JPEG_VALUE]
  )
  fun getBookPageThumbnail(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    request: WebRequest,
    @PathVariable bookId: String,
    @PathVariable pageNumber: Int
  ): ResponseEntity<ByteArray> =
    bookRepository.findByIdOrNull(bookId)?.let { book ->
      val media = mediaRepository.findById(bookId)
      if (request.checkNotModified(getBookLastModified(media))) {
        return@let ResponseEntity
          .status(HttpStatus.NOT_MODIFIED)
          .setNotModified(media)
          .body(ByteArray(0))
      }
      if (!principal.user.canAccessBook(book)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
      try {
        val pageContent = bookLifecycle.getBookPage(book, pageNumber, resizeTo = 300)

        ResponseEntity.ok()
          .contentType(getMediaTypeOrDefault(pageContent.mediaType))
          .setNotModified(media)
          .body(pageContent.content)
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

  @PostMapping("api/v1/books/{bookId}/analyze")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun analyze(@PathVariable bookId: String) {
    bookRepository.findByIdOrNull(bookId)?.let { book ->
      taskReceiver.analyzeBook(book)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PostMapping("api/v1/books/{bookId}/metadata/refresh")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun refreshMetadata(@PathVariable bookId: String) {
    bookRepository.findByIdOrNull(bookId)?.let { book ->
      taskReceiver.refreshBookMetadata(book)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PatchMapping("api/v1/books/{bookId}/metadata")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun updateMetadata(
    @PathVariable bookId: String,
    @Parameter(description = "Metadata fields to update. Set a field to null to unset the metadata. You can omit fields you don't want to update.")
    @Valid @RequestBody newMetadata: BookMetadataUpdateDto,
    @AuthenticationPrincipal principal: KomgaPrincipal
  ) =
    bookMetadataRepository.findByIdOrNull(bookId)?.let { existing ->
      val updated = with(newMetadata) {
        existing.copy(
          title = title ?: existing.title,
          titleLock = titleLock ?: existing.titleLock,
          summary = summary ?: existing.summary,
          summaryLock = summaryLock ?: existing.summaryLock,
          number = number ?: existing.number,
          numberLock = numberLock ?: existing.numberLock,
          numberSort = numberSort ?: existing.numberSort,
          numberSortLock = numberSortLock ?: existing.numberSortLock,
          readingDirection = if (isSet("readingDirection")) readingDirection else existing.readingDirection,
          readingDirectionLock = readingDirectionLock ?: existing.readingDirectionLock,
          publisher = publisher ?: existing.publisher,
          publisherLock = publisherLock ?: existing.publisherLock,
          ageRating = if (isSet("ageRating")) ageRating else existing.ageRating,
          ageRatingLock = ageRatingLock ?: existing.ageRatingLock,
          releaseDate = if (isSet("releaseDate")) releaseDate else existing.releaseDate,
          releaseDateLock = releaseDateLock ?: existing.releaseDateLock,
          authors = if (isSet("authors")) {
            if (authors != null) authors!!.map { Author(it.name ?: "", it.role ?: "") } else emptyList()
          } else existing.authors,
          authorsLock = authorsLock ?: existing.authorsLock
        )
      }
      bookMetadataRepository.update(updated)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @PatchMapping("api/v1/books/{bookId}/read-progress")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun markReadProgress(
    @PathVariable bookId: String,
    @Parameter(description = "page can be omitted if completed is set to true. completed can be omitted, and will be set accordingly depending on the page passed and the total number of pages in the book.")
    @Valid @RequestBody readProgress: ReadProgressUpdateDto,
    @AuthenticationPrincipal principal: KomgaPrincipal
  ) {
    bookRepository.findByIdOrNull(bookId)?.let { book ->
      if (!principal.user.canAccessBook(book)) throw ResponseStatusException(HttpStatus.FORBIDDEN)

      try {
        if (readProgress.completed != null && readProgress.completed)
          bookLifecycle.markReadProgressCompleted(book.id, principal.user)
        else
          bookLifecycle.markReadProgress(book, principal.user, readProgress.page!!)
      } catch (e: IllegalArgumentException) {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
      }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @DeleteMapping("api/v1/books/{bookId}/read-progress")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun deleteReadProgress(
    @PathVariable bookId: String,
    @AuthenticationPrincipal principal: KomgaPrincipal
  ) {
    bookRepository.findByIdOrNull(bookId)?.let { book ->
      if (!principal.user.canAccessBook(book)) throw ResponseStatusException(HttpStatus.FORBIDDEN)

      bookLifecycle.deleteReadProgress(book.id, principal.user)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }


  private fun ResponseEntity.BodyBuilder.setCachePrivate() =
    this.cacheControl(CacheControl.maxAge(0, TimeUnit.SECONDS)
      .cachePrivate()
      .mustRevalidate()
    )

  private fun ResponseEntity.BodyBuilder.setNotModified(media: Media) =
    this.setCachePrivate().lastModified(getBookLastModified(media))

  private fun getBookLastModified(media: Media) =
    media.lastModifiedDate.toInstant(ZoneOffset.UTC).toEpochMilli()


  private fun getMediaTypeOrDefault(mediaTypeString: String?): MediaType {
    mediaTypeString?.let {
      try {
        return MediaType.parseMediaType(mediaTypeString)
      } catch (ex: Exception) {
      }
    }
    return MediaType.APPLICATION_OCTET_STREAM
  }
}

