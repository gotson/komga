package org.gotson.komga.interfaces.rest

import com.github.klinq.jpaspec.`in`
import com.github.klinq.jpaspec.likeLower
import mu.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaNotReadyException
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.service.AsyncOrchestrator
import org.gotson.komga.domain.service.BookLifecycle
import org.gotson.komga.infrastructure.image.ImageType
import org.gotson.komga.infrastructure.security.KomgaPrincipal
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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
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

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class BookController(
  private val seriesRepository: SeriesRepository,
  private val bookRepository: BookRepository,
  private val bookLifecycle: BookLifecycle,
  private val asyncOrchestrator: AsyncOrchestrator
) {

  @GetMapping("api/v1/books")
  fun getAllBooks(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String?,
    @RequestParam(name = "library_id", required = false) libraryIds: List<Long>?,
    page: Pageable
  ): Page<BookDto> {
    val pageRequest = PageRequest.of(
      page.pageNumber,
      page.pageSize,
      if (page.sort.isSorted) page.sort
      else Sort.by(Sort.Order.asc("name").ignoreCase())
    )

    return mutableListOf<Specification<Book>>().let { specs ->
      when {
        !principal.user.sharedAllLibraries && !libraryIds.isNullOrEmpty() -> {
          val authorizedLibraryIDs = libraryIds.intersect(principal.user.sharedLibraries.map { it.id })
          if (authorizedLibraryIDs.isEmpty()) return@let Page.empty<Book>(pageRequest)
          else specs.add(Book::series.`in`(seriesRepository.findByLibraryIdIn(authorizedLibraryIDs)))
        }

        !principal.user.sharedAllLibraries -> specs.add(Book::series.`in`(seriesRepository.findByLibraryIn(principal.user.sharedLibraries)))

        !libraryIds.isNullOrEmpty() -> {
          specs.add(Book::series.`in`(seriesRepository.findByLibraryIdIn(libraryIds)))
        }
      }

      if (!searchTerm.isNullOrEmpty()) {
        specs.add(Book::name.likeLower("%$searchTerm%"))
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
      } catch (ex: MediaNotReadyException) {
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "Book analysis failed")
      } catch (ex: NoSuchFileException) {
        logger.warn(ex) { "File not found: $book" }
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "File not found, it may have moved")
      }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @PostMapping("api/v1/books/{bookId}/analyze")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
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

