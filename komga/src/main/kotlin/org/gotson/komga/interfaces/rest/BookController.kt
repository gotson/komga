package org.gotson.komga.interfaces.rest

import com.github.klinq.jpaspec.`in`
import com.github.klinq.jpaspec.likeLower
import com.github.klinq.jpaspec.toJoin
import mu.KotlinLogging
import org.gotson.komga.application.service.AsyncOrchestrator
import org.gotson.komga.application.service.BookLifecycle
import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.ImageConversionException
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaNotReadyException
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.infrastructure.image.ImageType
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.interfaces.rest.dto.BookDto
import org.gotson.komga.interfaces.rest.dto.BookMetadataUpdateDto
import org.gotson.komga.interfaces.rest.dto.PageDto
import org.gotson.komga.interfaces.rest.dto.toDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.CacheControl
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
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
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.NoSuchFileException
import java.time.ZoneOffset
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit
import javax.persistence.criteria.JoinType
import javax.validation.Valid

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class BookController(
  private val bookRepository: BookRepository,
  private val bookLifecycle: BookLifecycle,
  private val asyncOrchestrator: AsyncOrchestrator
) {

  @GetMapping("api/v1/books")
  fun getAllBooks(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String?,
    @RequestParam(name = "library_id", required = false) libraryIds: List<Long>?,
    @RequestParam(name = "media_status", required = false) mediaStatus: List<Media.Status>?,
    page: Pageable
  ): Page<BookDto> {
    val pageRequest = PageRequest.of(
      page.pageNumber,
      page.pageSize,
      if (page.sort.isSorted) Sort.by(page.sort.map { it.ignoreCase() }.toList())
      else Sort.by(Sort.Order.asc("metadata.title").ignoreCase())
    )

    return mutableListOf<Specification<Book>>().let { specs ->
      when {
        // limited user & libraryIds are specified: filter on provided libraries intersecting user's authorized libraries
        !principal.user.sharedAllLibraries && !libraryIds.isNullOrEmpty() -> {
          val authorizedLibraryIDs = libraryIds.intersect(principal.user.sharedLibraries.map { it.id })
          if (authorizedLibraryIDs.isEmpty()) return@let Page.empty<Book>(pageRequest)
          else specs.add(Book::series.toJoin().join(Series::library, JoinType.INNER).where(Library::id).`in`(authorizedLibraryIDs))
        }

        // limited user: filter on user's authorized libraries
        !principal.user.sharedAllLibraries -> specs.add(Book::series.toJoin().where(Series::library).`in`(principal.user.sharedLibraries))

        // non-limited user: filter on provided libraries
        !libraryIds.isNullOrEmpty() -> {
          specs.add(Book::series.toJoin().join(Series::library, JoinType.INNER).where(Library::id).`in`(libraryIds))
        }
      }

      if (!searchTerm.isNullOrEmpty()) {
        specs.add(Book::name.likeLower("%$searchTerm%"))
      }

      if (!mediaStatus.isNullOrEmpty()) {
        specs.add(Book::media.toJoin().where(Media::status).`in`(mediaStatus))
      }

      if (specs.isNotEmpty()) {
        bookRepository.findAll(specs.reduce { acc, spec -> acc.and(spec)!! }, pageRequest)
      } else {
        bookRepository.findAll(pageRequest)
      }
    }.map { it.toDto(includeFullUrl = principal.user.isAdmin()) }
  }


  @GetMapping("api/v1/books/latest")
  fun getLatestSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    page: Pageable
  ): Page<BookDto> {
    val pageRequest = PageRequest.of(
      page.pageNumber,
      page.pageSize,
      Sort.by(Sort.Direction.DESC, "lastModifiedDate")
    )

    return if (principal.user.sharedAllLibraries) {
      bookRepository.findAll(pageRequest)
    } else {
      bookRepository.findBySeriesLibraryIn(principal.user.sharedLibraries, pageRequest)
    }.map { it.toDto(includeFullUrl = principal.user.isAdmin()) }
  }


  @GetMapping("api/v1/books/{bookId}")
  fun getOneBook(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: Long
  ): BookDto =
    bookRepository.findByIdOrNull(bookId)?.let {
      if (!principal.user.canAccessBook(it)) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
      it.toDto(includeFullUrl = principal.user.isAdmin())
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @GetMapping("api/v1/books/{bookId}/previous")
  fun getBookSiblingPrevious(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: Long
  ): BookDto =
    bookRepository.findByIdOrNull(bookId)?.let { book ->
      if (!principal.user.canAccessBook(book)) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)

      val previousBook = book.series.books
        .sortedByDescending { it.number }
        .find { it.number < book.number }

      previousBook?.toDto(includeFullUrl = principal.user.isAdmin())
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @GetMapping("api/v1/books/{bookId}/next")
  fun getBookSiblingNext(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: Long
  ): BookDto =
    bookRepository.findByIdOrNull(bookId)?.let { book ->
      if (!principal.user.canAccessBook(book)) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)

      val nextBook = book.series.books
        .sortedBy { it.number }
        .find { it.number > book.number }

      nextBook?.toDto(includeFullUrl = principal.user.isAdmin()) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)


  @GetMapping(value = [
    "api/v1/books/{bookId}/thumbnail",
    "opds/v1.2/books/{bookId}/thumbnail"
  ], produces = [MediaType.IMAGE_PNG_VALUE])
  fun getBookThumbnail(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    request: WebRequest,
    @PathVariable bookId: Long
  ): ResponseEntity<ByteArray> =
    bookRepository.findByIdOrNull(bookId)?.let { book ->
      if (request.checkNotModified(getBookLastModified(book))) {
        return@let ResponseEntity
          .status(HttpStatus.NOT_MODIFIED)
          .setNotModified(book)
          .body(ByteArray(0))
      }
      if (!principal.user.canAccessBook(book)) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
      if (book.media.thumbnail != null) {
        ResponseEntity.ok()
          .setNotModified(book)
          .body(book.media.thumbnail)
      } else throw ResponseStatusException(HttpStatus.NOT_FOUND)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @GetMapping(value = [
    "api/v1/books/{bookId}/file",
    "api/v1/books/{bookId}/file/*",
    "opds/v1.2/books/{bookId}/file/*"
  ])
  fun getBookFile(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: Long
  ): ResponseEntity<ByteArray> =
    bookRepository.findByIdOrNull(bookId)?.let { book ->
      if (!principal.user.canAccessBook(book)) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
      try {
        ResponseEntity.ok()
          .headers(HttpHeaders().apply {
            contentDisposition = ContentDisposition.builder("attachment")
              .filename(book.fileName())
              .build()
          })
          .contentType(getMediaTypeOrDefault(book.media.mediaType))
          .body(File(book.url.toURI()).readBytes())
      } catch (ex: FileNotFoundException) {
        logger.warn(ex) { "File not found: $book" }
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "File not found, it may have moved")
      }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)


  @GetMapping("api/v1/books/{bookId}/pages")
  fun getBookPages(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: Long
  ): List<PageDto> =
    bookRepository.findByIdOrNull((bookId))?.let {
      if (!principal.user.canAccessBook(it)) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
      if (it.media.status == Media.Status.UNKNOWN) throw ResponseStatusException(HttpStatus.NOT_FOUND, "Book has not been analyzed yet")
      if (it.media.status in listOf(Media.Status.ERROR, Media.Status.UNSUPPORTED)) throw ResponseStatusException(HttpStatus.NOT_FOUND, "Book analysis failed")

      it.media.pages.mapIndexed { index, s -> PageDto(index + 1, s.fileName, s.mediaType) }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @GetMapping(value = [
    "api/v1/books/{bookId}/pages/{pageNumber}",
    "opds/v1.2/books/{bookId}/pages/{pageNumber}"
  ])
  fun getBookPage(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    request: WebRequest,
    @PathVariable bookId: Long,
    @PathVariable pageNumber: Int,
    @RequestParam(value = "convert", required = false) convertTo: String?,
    @RequestParam(value = "zero_based", defaultValue = "false") zeroBasedIndex: Boolean
  ): ResponseEntity<ByteArray> =
    bookRepository.findByIdOrNull((bookId))?.let { book ->
      if (request.checkNotModified(getBookLastModified(book))) {
        return@let ResponseEntity
          .status(HttpStatus.NOT_MODIFIED)
          .setNotModified(book)
          .body(ByteArray(0))
      }
      if (!principal.user.canAccessBook(book)) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
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
          .setNotModified(book)
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

  @GetMapping("api/v1/books/{bookId}/pages/{pageNumber}/thumbnail")
  fun getBookPageThumbnail(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    request: WebRequest,
    @PathVariable bookId: Long,
    @PathVariable pageNumber: Int
  ): ResponseEntity<ByteArray> =
    bookRepository.findByIdOrNull((bookId))?.let { book ->
      if (request.checkNotModified(getBookLastModified(book))) {
        return@let ResponseEntity
          .status(HttpStatus.NOT_MODIFIED)
          .setNotModified(book)
          .body(ByteArray(0))
      }
      if (!principal.user.canAccessBook(book)) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
      try {
        val pageContent = bookLifecycle.getBookPage(book, pageNumber, resizeTo = 300)

        ResponseEntity.ok()
          .contentType(getMediaTypeOrDefault(pageContent.mediaType))
          .setNotModified(book)
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
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun analyze(@PathVariable bookId: Long) {
    bookRepository.findByIdOrNull(bookId)?.let { book ->
      try {
        asyncOrchestrator.reAnalyzeBooks(listOf(book))
      } catch (e: RejectedExecutionException) {
        throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Another book analysis task is already running")
      }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PatchMapping("api/v1/books/{bookId}/metadata")
  @PreAuthorize("hasRole('ADMIN')")
  fun updateMetadata(
    @PathVariable bookId: Long,
    @Valid @RequestBody newMetadata: BookMetadataUpdateDto
  ): BookDto =
    bookRepository.findByIdOrNull(bookId)?.let { book ->
      with(newMetadata) {
        title?.let { book.metadata.title = it }
        titleLock?.let { book.metadata.titleLock = it }
        summary?.let { book.metadata.summary = it }
        summaryLock?.let { book.metadata.summaryLock = it }
        number?.let { book.metadata.number = it }
        numberLock?.let { book.metadata.numberLock = it }
        numberSort?.let { book.metadata.numberSort = it }
        numberSortLock?.let { book.metadata.numberSortLock = it }
        if (isSet("readingDirection")) book.metadata.readingDirection = newMetadata.readingDirection
        readingDirectionLock?.let { book.metadata.readingDirectionLock = it }
        publisher?.let { book.metadata.publisher = it }
        publisherLock?.let { book.metadata.publisherLock = it }
        if (isSet("ageRating")) book.metadata.ageRating = newMetadata.ageRating
        ageRatingLock?.let { book.metadata.ageRatingLock = it }
        if (isSet("releaseDate")) {
          book.metadata.releaseDate = newMetadata.releaseDate
        }
        releaseDateLock?.let { book.metadata.releaseDateLock = it }
        if (authors != null) {
          book.metadata.authors = authors!!.map {
            Author(it.name ?: "", it.role ?: "")
          }.toMutableList()
        } else book.metadata.authors = mutableListOf()
        authorsLock?.let { book.metadata.authorsLock = it }
      }
      bookRepository.save(book).toDto(includeFullUrl = true)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  private fun ResponseEntity.BodyBuilder.setNotModified(book: Book) =
    this.cacheControl(CacheControl.maxAge(0, TimeUnit.SECONDS)
      .cachePrivate()
      .mustRevalidate()
    ).lastModified(getBookLastModified(book))

  private fun getBookLastModified(book: Book) =
    book.media.lastModifiedDate!!.toInstant(ZoneOffset.UTC).toEpochMilli()


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

