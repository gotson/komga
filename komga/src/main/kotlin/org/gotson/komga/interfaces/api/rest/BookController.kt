package org.gotson.komga.interfaces.api.rest

import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.gotson.komga.application.tasks.HIGHEST_PRIORITY
import org.gotson.komga.application.tasks.HIGH_PRIORITY
import org.gotson.komga.application.tasks.LOWEST_PRIORITY
import org.gotson.komga.application.tasks.TaskEmitter
import org.gotson.komga.domain.model.BookSearchWithReadProgress
import org.gotson.komga.domain.model.Dimension
import org.gotson.komga.domain.model.DomainEvent
import org.gotson.komga.domain.model.ImageConversionException
import org.gotson.komga.domain.model.MarkSelectedPreference
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaExtensionEpub
import org.gotson.komga.domain.model.MediaNotReadyException
import org.gotson.komga.domain.model.MediaProfile
import org.gotson.komga.domain.model.R2Progression
import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.domain.model.ROLE_PAGE_STREAMING
import org.gotson.komga.domain.model.ReadStatus
import org.gotson.komga.domain.model.ThumbnailBook
import org.gotson.komga.domain.model.toR2Progression
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.domain.persistence.ReadProgressRepository
import org.gotson.komga.domain.persistence.ThumbnailBookRepository
import org.gotson.komga.domain.service.BookAnalyzer
import org.gotson.komga.domain.service.BookLifecycle
import org.gotson.komga.infrastructure.image.ImageAnalyzer
import org.gotson.komga.infrastructure.jooq.UnpagedSorted
import org.gotson.komga.infrastructure.mediacontainer.ContentDetector
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.infrastructure.swagger.PageableAsQueryParam
import org.gotson.komga.infrastructure.swagger.PageableWithoutSortAsQueryParam
import org.gotson.komga.infrastructure.web.getMediaTypeOrDefault
import org.gotson.komga.interfaces.api.CommonBookController
import org.gotson.komga.interfaces.api.ContentRestrictionChecker
import org.gotson.komga.interfaces.api.WebPubGenerator
import org.gotson.komga.interfaces.api.dto.MEDIATYPE_DIVINA_JSON_VALUE
import org.gotson.komga.interfaces.api.dto.MEDIATYPE_POSITION_LIST_JSON
import org.gotson.komga.interfaces.api.dto.MEDIATYPE_POSITION_LIST_JSON_VALUE
import org.gotson.komga.interfaces.api.dto.MEDIATYPE_PROGRESSION_JSON_VALUE
import org.gotson.komga.interfaces.api.dto.MEDIATYPE_WEBPUB_JSON_VALUE
import org.gotson.komga.interfaces.api.dto.WPPublicationDto
import org.gotson.komga.interfaces.api.getBookLastModified
import org.gotson.komga.interfaces.api.persistence.BookDtoRepository
import org.gotson.komga.interfaces.api.rest.dto.BookDto
import org.gotson.komga.interfaces.api.rest.dto.BookImportBatchDto
import org.gotson.komga.interfaces.api.rest.dto.BookMetadataUpdateDto
import org.gotson.komga.interfaces.api.rest.dto.PageDto
import org.gotson.komga.interfaces.api.rest.dto.R2Positions
import org.gotson.komga.interfaces.api.rest.dto.ReadListDto
import org.gotson.komga.interfaces.api.rest.dto.ReadProgressUpdateDto
import org.gotson.komga.interfaces.api.rest.dto.ThumbnailBookDto
import org.gotson.komga.interfaces.api.rest.dto.patch
import org.gotson.komga.interfaces.api.rest.dto.restrictUrl
import org.gotson.komga.interfaces.api.rest.dto.toDto
import org.gotson.komga.interfaces.api.setNotModified
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.format.annotation.DateTimeFormat
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
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.nio.file.NoSuchFileException
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class BookController(
  private val taskEmitter: TaskEmitter,
  private val bookAnalyzer: BookAnalyzer,
  private val bookLifecycle: BookLifecycle,
  private val bookRepository: BookRepository,
  private val readProgressRepository: ReadProgressRepository,
  private val bookMetadataRepository: BookMetadataRepository,
  private val mediaRepository: MediaRepository,
  private val bookDtoRepository: BookDtoRepository,
  private val readListRepository: ReadListRepository,
  private val contentDetector: ContentDetector,
  private val imageAnalyzer: ImageAnalyzer,
  private val eventPublisher: ApplicationEventPublisher,
  private val thumbnailBookRepository: ThumbnailBookRepository,
  private val webPubGenerator: WebPubGenerator,
  private val contentRestrictionChecker: ContentRestrictionChecker,
  private val commonBookController: CommonBookController,
) {
  @PageableAsQueryParam
  @GetMapping("api/v1/books")
  fun getAllBooks(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String? = null,
    @RequestParam(name = "library_id", required = false) libraryIds: List<String>? = null,
    @RequestParam(name = "media_status", required = false) mediaStatus: List<Media.Status>? = null,
    @RequestParam(name = "read_status", required = false) readStatus: List<ReadStatus>? = null,
    @RequestParam(name = "released_after", required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    releasedAfter: LocalDate? = null,
    @RequestParam(name = "tag", required = false) tags: List<String>? = null,
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable,
  ): Page<BookDto> {
    val sort =
      when {
        page.sort.isSorted -> page.sort
        !searchTerm.isNullOrBlank() -> Sort.by("relevance")
        else -> Sort.unsorted()
      }

    val pageRequest =
      if (unpaged)
        UnpagedSorted(sort)
      else
        PageRequest.of(
          page.pageNumber,
          page.pageSize,
          sort,
        )

    val bookSearch =
      BookSearchWithReadProgress(
        libraryIds = principal.user.getAuthorizedLibraryIds(libraryIds),
        searchTerm = searchTerm,
        mediaStatus = mediaStatus,
        readStatus = readStatus,
        releasedAfter = releasedAfter,
        tags = tags,
      )

    return bookDtoRepository.findAll(bookSearch, principal.user.id, pageRequest, principal.user.restrictions)
      .map { it.restrictUrl(!principal.user.roleAdmin) }
  }

  @Operation(description = "Return newly added or updated books.")
  @PageableWithoutSortAsQueryParam
  @GetMapping("api/v1/books/latest")
  fun getLatestBooks(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable,
  ): Page<BookDto> {
    val sort = Sort.by(Sort.Order.desc("lastModifiedDate"))

    val pageRequest =
      if (unpaged)
        UnpagedSorted(sort)
      else
        PageRequest.of(
          page.pageNumber,
          page.pageSize,
          sort,
        )

    return bookDtoRepository.findAll(
      BookSearchWithReadProgress(
        libraryIds = principal.user.getAuthorizedLibraryIds(null),
      ),
      principal.user.id,
      pageRequest,
      principal.user.restrictions,
    ).map { it.restrictUrl(!principal.user.roleAdmin) }
  }

  @Operation(description = "Return first unread book of series with at least one book read and no books in progress.")
  @PageableWithoutSortAsQueryParam
  @GetMapping("api/v1/books/ondeck")
  fun getBooksOnDeck(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "library_id", required = false) libraryIds: List<String>? = null,
    @Parameter(hidden = true) page: Pageable,
  ): Page<BookDto> =
    bookDtoRepository.findAllOnDeck(
      principal.user.id,
      principal.user.getAuthorizedLibraryIds(libraryIds),
      page,
      principal.user.restrictions,
    ).map { it.restrictUrl(!principal.user.roleAdmin) }

  @PageableAsQueryParam
  @GetMapping("api/v1/books/duplicates")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  fun getDuplicateBooks(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable,
  ): Page<BookDto> {
    val sort =
      when {
        page.sort.isSorted -> page.sort
        else -> Sort.by(Sort.Order.asc("fileHash"))
      }

    val pageRequest =
      if (unpaged)
        Pageable.unpaged()
      else
        PageRequest.of(
          page.pageNumber,
          page.pageSize,
          sort,
        )

    return bookDtoRepository.findAllDuplicates(principal.user.id, pageRequest)
  }

  @GetMapping("api/v1/books/{bookId}")
  fun getOneBook(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String,
  ): BookDto =
    bookDtoRepository.findByIdOrNull(bookId, principal.user.id)?.let {
      contentRestrictionChecker.checkContentRestriction(principal.user, it)

      it.restrictUrl(!principal.user.roleAdmin)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @GetMapping("api/v1/books/{bookId}/previous")
  fun getBookSiblingPrevious(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String,
  ): BookDto {
    contentRestrictionChecker.checkContentRestriction(principal.user, bookId)

    return bookDtoRepository.findPreviousInSeriesOrNull(bookId, principal.user.id)
      ?.restrictUrl(!principal.user.roleAdmin)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @GetMapping("api/v1/books/{bookId}/next")
  fun getBookSiblingNext(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String,
  ): BookDto {
    contentRestrictionChecker.checkContentRestriction(principal.user, bookId)

    return bookDtoRepository.findNextInSeriesOrNull(bookId, principal.user.id)
      ?.restrictUrl(!principal.user.roleAdmin)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @GetMapping("api/v1/books/{bookId}/readlists")
  fun getAllReadListsByBook(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "bookId") bookId: String,
  ): List<ReadListDto> {
    contentRestrictionChecker.checkContentRestriction(principal.user, bookId)

    return readListRepository.findAllContainingBookId(bookId, principal.user.getAuthorizedLibraryIds(null), principal.user.restrictions)
      .map { it.toDto() }
  }

  @ApiResponse(content = [Content(schema = Schema(type = "string", format = "binary"))])
  @GetMapping(
    value = ["api/v1/books/{bookId}/thumbnail"],
    produces = [MediaType.IMAGE_JPEG_VALUE],
  )
  fun getBookThumbnail(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String,
  ): ByteArray {
    contentRestrictionChecker.checkContentRestriction(principal.user, bookId)

    return bookLifecycle.getThumbnailBytes(bookId)?.bytes ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @ApiResponse(content = [Content(schema = Schema(type = "string", format = "binary"))])
  @GetMapping(value = ["api/v1/books/{bookId}/thumbnails/{thumbnailId}"], produces = [MediaType.IMAGE_JPEG_VALUE])
  fun getBookThumbnailById(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "bookId") bookId: String,
    @PathVariable(name = "thumbnailId") thumbnailId: String,
  ): ByteArray {
    contentRestrictionChecker.checkContentRestriction(principal.user, bookId)

    return bookLifecycle.getThumbnailBytesByThumbnailId(thumbnailId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @GetMapping(value = ["api/v1/books/{bookId}/thumbnails"], produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getBookThumbnails(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "bookId") bookId: String,
  ): Collection<ThumbnailBookDto> {
    contentRestrictionChecker.checkContentRestriction(principal.user, bookId)

    return thumbnailBookRepository.findAllByBookId(bookId)
      .map { it.toDto() }
  }

  @PostMapping(value = ["api/v1/books/{bookId}/thumbnails"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  fun addUserUploadedBookThumbnail(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "bookId") bookId: String,
    @RequestParam("file") file: MultipartFile,
    @RequestParam("selected") selected: Boolean = true,
  ): ThumbnailBookDto {
    val book = bookRepository.findByIdOrNull(bookId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val mediaType = file.inputStream.buffered().use { contentDetector.detectMediaType(it) }
    if (!contentDetector.isImage(mediaType))
      throw ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE)

    return bookLifecycle.addThumbnailForBook(
      ThumbnailBook(
        bookId = book.id,
        thumbnail = file.bytes,
        type = ThumbnailBook.Type.USER_UPLOADED,
        selected = selected,
        fileSize = file.bytes.size.toLong(),
        mediaType = mediaType,
        dimension = imageAnalyzer.getDimension(file.inputStream.buffered()) ?: Dimension(0, 0),
      ),
      if (selected) MarkSelectedPreference.YES else MarkSelectedPreference.NO,
    ).toDto()
  }

  @PutMapping("api/v1/books/{bookId}/thumbnails/{thumbnailId}/selected")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun markSelectedBookThumbnail(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "bookId") bookId: String,
    @PathVariable(name = "thumbnailId") thumbnailId: String,
  ) {
    thumbnailBookRepository.findByIdOrNull(thumbnailId)?.let {
      thumbnailBookRepository.markSelected(it)
      eventPublisher.publishEvent(DomainEvent.ThumbnailBookAdded(it.copy(selected = true)))
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @DeleteMapping("api/v1/books/{bookId}/thumbnails/{thumbnailId}")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun deleteUserUploadedBookThumbnail(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "bookId") bookId: String,
    @PathVariable(name = "thumbnailId") thumbnailId: String,
  ) {
    thumbnailBookRepository.findByIdOrNull(thumbnailId)?.let {
      try {
        bookLifecycle.deleteThumbnailForBook(it)
      } catch (e: IllegalArgumentException) {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
      }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @GetMapping("api/v1/books/{bookId}/pages")
  fun getBookPages(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String,
  ): List<PageDto> =
    bookRepository.findByIdOrNull(bookId)?.let { book ->
      contentRestrictionChecker.checkContentRestriction(principal.user, book)

      val media = mediaRepository.findById(book.id)
      when (media.status) {
        Media.Status.UNKNOWN -> throw ResponseStatusException(HttpStatus.NOT_FOUND, "Book has not been analyzed yet")
        Media.Status.OUTDATED -> throw ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Book is outdated and must be re-analyzed",
        )

        Media.Status.ERROR -> throw ResponseStatusException(HttpStatus.NOT_FOUND, "Book analysis failed")
        Media.Status.UNSUPPORTED -> throw ResponseStatusException(HttpStatus.NOT_FOUND, "Book format is not supported")
        Media.Status.READY -> {
          val pages = if (media.profile == MediaProfile.PDF) bookAnalyzer.getPdfPagesDynamic(media) else media.pages
          pages.mapIndexed { index, bookPage ->
            PageDto(
              number = index + 1,
              fileName = bookPage.fileName,
              mediaType = bookPage.mediaType,
              width = bookPage.dimension?.width,
              height = bookPage.dimension?.height,
              sizeBytes = bookPage.fileSize,
            )
          }
        }
      }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @ApiResponse(content = [Content(mediaType = "image/*", schema = Schema(type = "string", format = "binary"))])
  @GetMapping(
    value = ["api/v1/books/{bookId}/pages/{pageNumber}"],
    produces = [MediaType.ALL_VALUE],
  )
  @PreAuthorize("hasRole('$ROLE_PAGE_STREAMING')")
  fun getBookPage(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    request: ServletWebRequest,
    @PathVariable bookId: String,
    @PathVariable pageNumber: Int,
    @Parameter(
      description = "Convert the image to the provided format.",
      schema = Schema(allowableValues = ["jpeg", "png"]),
    )
    @RequestParam(value = "convert", required = false)
    convertTo: String?,
    @Parameter(description = "If set to true, pages will start at index 0. If set to false, pages will start at index 1.")
    @RequestParam(value = "zero_based", defaultValue = "false")
    zeroBasedIndex: Boolean,
    @Parameter(description = "Some very limited server driven content negotiation is handled. If a book is a PDF book, and the Accept header contains 'application/pdf' as a more specific type than other 'image/' types, a raw PDF page will be returned.")
    @RequestHeader(HttpHeaders.ACCEPT, required = false)
    acceptHeaders: MutableList<MediaType>?,
    @RequestParam(value = "contentNegotiation", defaultValue = "true")
    contentNegotiation: Boolean,
  ): ResponseEntity<ByteArray> =
    commonBookController.getBookPageInternal(bookId, if (zeroBasedIndex) pageNumber + 1 else pageNumber, convertTo, request, principal, if (contentNegotiation) acceptHeaders else null)

  @ApiResponse(content = [Content(schema = Schema(type = "string", format = "binary"))])
  @GetMapping(
    value = ["api/v1/books/{bookId}/pages/{pageNumber}/thumbnail"],
    produces = [MediaType.IMAGE_JPEG_VALUE],
  )
  fun getBookPageThumbnail(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    request: WebRequest,
    @PathVariable bookId: String,
    @PathVariable pageNumber: Int,
  ): ResponseEntity<ByteArray> =
    bookRepository.findByIdOrNull(bookId)?.let { book ->
      val media = mediaRepository.findById(bookId)
      if (request.checkNotModified(getBookLastModified(media))) {
        return@let ResponseEntity
          .status(HttpStatus.NOT_MODIFIED)
          .setNotModified(media)
          .body(ByteArray(0))
      }

      contentRestrictionChecker.checkContentRestriction(principal.user, book)

      try {
        val pageContent = bookLifecycle.getBookPage(book, pageNumber, resizeTo = 300)

        ResponseEntity.ok()
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
    value = ["api/v1/books/{bookId}/manifest"],
    produces = [MEDIATYPE_WEBPUB_JSON_VALUE, MEDIATYPE_DIVINA_JSON_VALUE],
  )
  fun getWebPubManifest(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String,
  ): ResponseEntity<WPPublicationDto> {
    val manifest = commonBookController.getWebPubManifestInternal(principal, bookId, webPubGenerator)
    return ResponseEntity.ok()
      .contentType(manifest.mediaType)
      .body(manifest)
  }

  @GetMapping(
    value = ["api/v1/books/{bookId}/positions"],
    produces = [MEDIATYPE_POSITION_LIST_JSON_VALUE],
  )
  fun getPositions(
    request: HttpServletRequest,
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String,
  ): ResponseEntity<R2Positions> =
    bookRepository.findByIdOrNull(bookId)?.let { book ->
      val media = mediaRepository.findById(book.id)

      if (ServletWebRequest(request).checkNotModified(getBookLastModified(media))) {
        return ResponseEntity
          .status(HttpStatus.NOT_MODIFIED)
          .setNotModified(media)
          .body(null)
      }

      contentRestrictionChecker.checkContentRestriction(principal.user, book)

      val extension =
        mediaRepository.findExtensionByIdOrNull(book.id) as? MediaExtensionEpub
          ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

      ResponseEntity.ok()
        .contentType(MEDIATYPE_POSITION_LIST_JSON)
        .setNotModified(media)
        .body(R2Positions(extension.positions.size, extension.positions))
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @GetMapping(
    value = ["api/v1/books/{bookId}/progression"],
    produces = [MEDIATYPE_PROGRESSION_JSON_VALUE],
  )
  fun getProgression(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String,
  ): ResponseEntity<R2Progression> =
    bookRepository.findByIdOrNull(bookId)?.let { book ->
      contentRestrictionChecker.checkContentRestriction(principal.user, book)

      readProgressRepository.findByBookIdAndUserIdOrNull(bookId, principal.user.id)?.let {
        ResponseEntity.ok(it.toR2Progression())
      } ?: ResponseEntity.noContent().build()
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @PutMapping("api/v1/books/{bookId}/progression")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun markProgression(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String,
    @RequestBody progression: R2Progression,
  ) {
    bookRepository.findByIdOrNull(bookId)?.let { book ->
      contentRestrictionChecker.checkContentRestriction(principal.user, book)

      try {
        bookLifecycle.markProgression(book, principal.user, progression)
      } catch (e: IllegalStateException) {
        throw ResponseStatusException(HttpStatus.CONFLICT, e.message)
      } catch (e: IllegalArgumentException) {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
      }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @GetMapping(
    value = ["api/v1/books/{bookId}/manifest/epub"],
    produces = [MEDIATYPE_WEBPUB_JSON_VALUE],
  )
  fun getWebPubManifestEpub(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String,
  ): WPPublicationDto =
    commonBookController.getWebPubManifestEpubInternal(principal, bookId, webPubGenerator)

  @GetMapping(
    value = ["api/v1/books/{bookId}/manifest/pdf"],
    produces = [MEDIATYPE_WEBPUB_JSON_VALUE],
  )
  fun getWebPubManifestPdf(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String,
  ): WPPublicationDto =
    commonBookController.getWebPubManifestPdfInternal(principal, bookId, webPubGenerator)

  @GetMapping(
    value = ["api/v1/books/{bookId}/manifest/divina"],
    produces = [MEDIATYPE_DIVINA_JSON_VALUE],
  )
  fun getWebPubManifestDivina(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String,
  ): WPPublicationDto =
    commonBookController.getWebPubManifestDivinaInternal(principal, bookId, webPubGenerator)

  @PostMapping("api/v1/books/{bookId}/analyze")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun analyze(
    @PathVariable bookId: String,
  ) {
    bookRepository.findByIdOrNull(bookId)?.let { book ->
      taskEmitter.analyzeBook(book, HIGH_PRIORITY)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PostMapping("api/v1/books/{bookId}/metadata/refresh")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun refreshMetadata(
    @PathVariable bookId: String,
  ) {
    bookRepository.findByIdOrNull(bookId)?.let { book ->
      taskEmitter.refreshBookMetadata(book, priority = HIGH_PRIORITY)
      taskEmitter.refreshBookLocalArtwork(book, priority = HIGH_PRIORITY)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PatchMapping("api/v1/books/{bookId}/metadata")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun updateMetadata(
    @PathVariable bookId: String,
    @Parameter(description = "Metadata fields to update. Set a field to null to unset the metadata. You can omit fields you don't want to update.")
    @Valid
    @RequestBody
    newMetadata: BookMetadataUpdateDto,
  ) =
    bookMetadataRepository.findByIdOrNull(bookId)?.let { existing ->
      val updated = existing.patch(newMetadata)
      bookMetadataRepository.update(updated)

      bookRepository.findByIdOrNull(bookId)?.let { updatedBook ->
        taskEmitter.aggregateSeriesMetadata(updatedBook.seriesId)
        updatedBook.let { eventPublisher.publishEvent(DomainEvent.BookUpdated(it)) }
      }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @PatchMapping("api/v1/books/metadata")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun updateBatchMetadata(
    @Parameter(description = "A map of book IDs which values are the metadata fields to update. Set a field to null to unset the metadata. You can omit fields you don't want to update.")
    @Valid
    @RequestBody
    newMetadatas: Map<String, BookMetadataUpdateDto>,
  ) {
    val updatedBooks =
      newMetadatas.mapNotNull { (bookId, newMetadata) ->
        bookMetadataRepository.findByIdOrNull(bookId)?.let { existing ->
          val updated = existing.patch(newMetadata)
          bookMetadataRepository.update(updated)

          bookRepository.findByIdOrNull(bookId)
        }
      }

    updatedBooks.forEach { eventPublisher.publishEvent(DomainEvent.BookUpdated(it)) }
    updatedBooks.map { it.seriesId }.distinct().forEach { taskEmitter.aggregateSeriesMetadata(it) }
  }

  @Operation(description = "Mark book as read and/or change page progress")
  @PatchMapping("api/v1/books/{bookId}/read-progress")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun markReadProgress(
    @PathVariable bookId: String,
    @Parameter(description = "page can be omitted if completed is set to true. completed can be omitted, and will be set accordingly depending on the page passed and the total number of pages in the book.")
    @Valid
    @RequestBody
    readProgress: ReadProgressUpdateDto,
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ) {
    bookRepository.findByIdOrNull(bookId)?.let { book ->
      contentRestrictionChecker.checkContentRestriction(principal.user, book)

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

  @Operation(description = "Mark book as unread")
  @DeleteMapping("api/v1/books/{bookId}/read-progress")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun deleteReadProgress(
    @PathVariable bookId: String,
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ) {
    bookRepository.findByIdOrNull(bookId)?.let { book ->
      contentRestrictionChecker.checkContentRestriction(principal.user, book)

      bookLifecycle.deleteReadProgress(book, principal.user)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PostMapping("api/v1/books/import")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun importBooks(
    @RequestBody bookImportBatch: BookImportBatchDto,
  ) {
    bookImportBatch.books.forEach {
      try {
        taskEmitter.importBook(
          sourceFile = it.sourceFile,
          seriesId = it.seriesId,
          copyMode = bookImportBatch.copyMode,
          destinationName = it.destinationName,
          upgradeBookId = it.upgradeBookId,
          priority = HIGHEST_PRIORITY,
        )
      } catch (e: Exception) {
        logger.error(e) { "Error while creating import task for: $it" }
      }
    }
  }

  @DeleteMapping("api/v1/books/{bookId}/file")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun deleteBook(
    @PathVariable bookId: String,
  ) {
    taskEmitter.deleteBook(
      bookId = bookId,
      priority = HIGHEST_PRIORITY,
    )
  }

  @PutMapping("api/v1/books/thumbnails")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun regenerateThumbnails(
    @RequestParam(name = "for_bigger_result_only", required = false) forBiggerResultOnly: Boolean = false,
  ) {
    taskEmitter.findBookThumbnailsToRegenerate(forBiggerResultOnly, LOWEST_PRIORITY)
  }
}
