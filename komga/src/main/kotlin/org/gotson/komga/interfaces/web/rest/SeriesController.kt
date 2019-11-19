package org.gotson.komga.interfaces.web.rest

import com.fasterxml.jackson.annotation.JsonFormat
import com.github.klinq.jpaspec.`in`
import com.github.klinq.jpaspec.likeLower
import mu.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.MetadataNotReadyException
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesRepository
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
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.NoSuchFileException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("api/v1/series", produces = [MediaType.APPLICATION_JSON_VALUE])
class SeriesController(
    private val seriesRepository: SeriesRepository,
    private val libraryRepository: LibraryRepository,
    private val bookRepository: BookRepository,
    private val bookLifecycle: BookLifecycle
) {

  @GetMapping
  fun getAllSeries(
      @AuthenticationPrincipal principal: KomgaPrincipal,
      @RequestParam(name = "search", required = false) searchTerm: String?,
      @RequestParam(name = "library_id", required = false) libraryIds: List<Long>?,
      page: Pageable
  ): Page<SeriesDto> {
    val pageRequest = PageRequest.of(
        page.pageNumber,
        page.pageSize,
        if (page.sort.isSorted) page.sort
        else Sort.by(Sort.Order.asc("name").ignoreCase())
    )

    return mutableListOf<Specification<Series>>().let { specs ->
      if (!principal.user.sharedAllLibraries) {
        specs.add(Series::library.`in`(principal.user.sharedLibraries))
      }

      if (!searchTerm.isNullOrEmpty()) {
        specs.add(Series::name.likeLower("%$searchTerm%"))
      }

      if (!libraryIds.isNullOrEmpty()) {
        val libraries = libraryRepository.findAllById(libraryIds)
        specs.add(Series::library.`in`(libraries))
      }

      if (specs.isNotEmpty()) {
        seriesRepository.findAll(specs.reduce { acc, spec -> acc.and(spec)!! }, pageRequest)
      } else {
        seriesRepository.findAll(pageRequest)
      }
    }.map { it.toDto() }
  }

  @GetMapping("/latest")
  fun getLatestSeries(
      @AuthenticationPrincipal principal: KomgaPrincipal,
      page: Pageable
  ): Page<SeriesDto> {
    val pageRequest = PageRequest.of(
        page.pageNumber,
        page.pageSize,
        Sort.by(Sort.Direction.DESC, "lastModifiedDate")
    )

    return if (principal.user.sharedAllLibraries) {
      seriesRepository.findAll(pageRequest)
    } else {
      seriesRepository.findByLibraryIn(principal.user.sharedLibraries, pageRequest)
    }.map { it.toDto() }
  }

  @GetMapping("{seriesId}")
  fun getOneSeries(
      @AuthenticationPrincipal principal: KomgaPrincipal,
      @PathVariable(name = "seriesId") id: Long
  ): SeriesDto =
      seriesRepository.findByIdOrNull(id)?.let {
        if (!principal.user.canAccessSeries(it)) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        it.toDto()
      } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  private val thumbnailsCache = CacheControl
      .maxAge(4, TimeUnit.HOURS)
      .cachePrivate()

  @GetMapping(value = ["{seriesId}/thumbnail"], produces = [MediaType.IMAGE_JPEG_VALUE])
  fun getSeriesThumbnail(
      @AuthenticationPrincipal principal: KomgaPrincipal,
      @PathVariable(name = "seriesId") id: Long
  ): ResponseEntity<ByteArray> =
      seriesRepository.findByIdOrNull(id)?.let { series ->
        if (!principal.user.canAccessSeries(series)) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)

        val thumbnail = series.books.firstOrNull()?.metadata?.thumbnail
        if (thumbnail != null) {
          ResponseEntity.ok()
              .cacheControl(thumbnailsCache)
              .body(thumbnail)
        } else throw ResponseStatusException(HttpStatus.NOT_FOUND)
      } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @GetMapping("{seriesId}/books")
  fun getAllBooksBySeries(
      @AuthenticationPrincipal principal: KomgaPrincipal,
      @PathVariable(name = "seriesId") id: Long,
      @RequestParam(value = "ready_only", defaultValue = "true") readyFilter: Boolean,
      page: Pageable
  ): Page<BookDto> {
    seriesRepository.findByIdOrNull(id)?.let {
      if (!principal.user.canAccessSeries(it)) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    return if (readyFilter) {
      bookRepository.findAllByMetadataStatusAndSeriesId(BookMetadata.Status.READY.name, id, page)
    } else {
      bookRepository.findAllBySeriesId(id, page)
    }.map { it.toDto() }
  }

  @GetMapping("{seriesId}/books/{bookId}")
  fun getOneBook(
      @AuthenticationPrincipal principal: KomgaPrincipal,
      @PathVariable seriesId: Long,
      @PathVariable bookId: Long
  ): BookDto {
    seriesRepository.findByIdOrNull(seriesId)?.let {
      if (!principal.user.canAccessSeries(it)) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    return bookRepository.findByIdOrNull(bookId)?.toDto() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @GetMapping(value = ["{seriesId}/books/{bookId}/thumbnail"], produces = [MediaType.IMAGE_PNG_VALUE])
  fun getBookThumbnail(
      @AuthenticationPrincipal principal: KomgaPrincipal,
      @PathVariable seriesId: Long,
      @PathVariable bookId: Long
  ): ResponseEntity<ByteArray> {
    seriesRepository.findByIdOrNull(seriesId)?.let {
      if (!principal.user.canAccessSeries(it)) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    return bookRepository.findByIdOrNull(bookId)?.let {
      if (it.metadata.thumbnail != null) {
        ResponseEntity.ok()
            .cacheControl(thumbnailsCache)
            .body(it.metadata.thumbnail)
      } else throw ResponseStatusException(HttpStatus.NOT_FOUND)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @GetMapping(value = [
    "{seriesId}/books/{bookId}/file",
    "{seriesId}/books/{bookId}/file/*"
  ])
  fun getBookFile(
      @AuthenticationPrincipal principal: KomgaPrincipal,
      @PathVariable seriesId: Long,
      @PathVariable bookId: Long
  ): ResponseEntity<ByteArray> {
    seriesRepository.findByIdOrNull(seriesId)?.let {
      if (!principal.user.canAccessSeries(it)) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    return bookRepository.findByIdOrNull(bookId)?.let { book ->
      try {
        ResponseEntity.ok()
            .headers(HttpHeaders().apply {
              contentDisposition = ContentDisposition.builder("attachment")
                  .filename(book.fileName())
                  .build()
            })
            .contentType(getMediaTypeOrDefault(book.metadata.mediaType))
            .body(File(book.url.toURI()).readBytes())
      } catch (ex: FileNotFoundException) {
        logger.warn(ex) { "File not found: $book" }
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "File not found, it may have moved")
      }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @GetMapping("{seriesId}/books/{bookId}/pages")
  fun getBookPages(
      @AuthenticationPrincipal principal: KomgaPrincipal,
      @PathVariable seriesId: Long,
      @PathVariable bookId: Long
  ): List<PageDto> {
    seriesRepository.findByIdOrNull(seriesId)?.let {
      if (!principal.user.canAccessSeries(it)) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    return bookRepository.findByIdOrNull((bookId))?.let {
      if (it.metadata.status == BookMetadata.Status.UNKNOWN) throw ResponseStatusException(HttpStatus.NOT_FOUND, "Book is not parsed yet")
      if (it.metadata.status in listOf(BookMetadata.Status.ERROR, BookMetadata.Status.UNSUPPORTED)) throw ResponseStatusException(HttpStatus.NOT_FOUND, "Book cannot be parsed")

      it.metadata.pages.mapIndexed { index, s -> PageDto(index + 1, s.fileName, s.mediaType) }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @GetMapping("{seriesId}/books/{bookId}/pages/{pageNumber}")
  fun getBookPage(
      @AuthenticationPrincipal principal: KomgaPrincipal,
      @PathVariable seriesId: Long,
      @PathVariable bookId: Long,
      @PathVariable pageNumber: Int,
      @RequestParam(value = "convert", required = false) convertTo: String?,
      @RequestParam(value = "zero_based", defaultValue = "false") zeroBasedIndex: Boolean
  ): ResponseEntity<ByteArray> {
    seriesRepository.findByIdOrNull(seriesId)?.let {
      if (!principal.user.canAccessSeries(it)) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    return bookRepository.findByIdOrNull((bookId))?.let { book ->
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
            .body(pageContent.content)
      } catch (ex: IndexOutOfBoundsException) {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Page number does not exist")
      } catch (ex: MetadataNotReadyException) {
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "Book cannot be parsed")
      } catch (ex: NoSuchFileException) {
        logger.warn(ex) { "File not found: $book" }
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "File not found, it may have moved")
      }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

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

data class SeriesDto(
    val id: Long,
    val name: String,
    val url: String,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val lastModified: LocalDateTime?
)

fun Series.toDto() = SeriesDto(
    id = id,
    name = name,
    url = url.toString(),
    lastModified = lastModifiedDate?.toUTC()
)


data class BookDto(
    val id: Long,
    val name: String,
    val url: String,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val lastModified: LocalDateTime?,
    val sizeBytes: Long,
    val size: String,
    val metadata: BookMetadataDto
)

data class BookMetadataDto(
    val status: String,
    val mediaType: String
)

fun Book.toDto() =
    BookDto(
        id = id,
        name = name,
        url = url.toURI().path,
        lastModified = lastModifiedDate?.toUTC(),
        sizeBytes = fileSize,
        size = fileSizeHumanReadable(),
        metadata = BookMetadataDto(
            status = metadata.status.toString(),
            mediaType = metadata.mediaType ?: ""
        )
    )

data class PageDto(
    val number: Int,
    val fileName: String,
    val mediaType: String
)

fun LocalDateTime.toUTC(): LocalDateTime =
    atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()
