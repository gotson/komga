package org.gotson.komga.interfaces.web.rest

import com.github.klinq.jpaspec.`in`
import com.github.klinq.jpaspec.likeLower
import mu.KotlinLogging
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.CacheControl
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
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("api/v1/series", produces = [MediaType.APPLICATION_JSON_VALUE])
class SeriesController(
    private val seriesRepository: SeriesRepository,
    private val libraryRepository: LibraryRepository,
    private val bookRepository: BookRepository
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
              .cacheControl(CacheControl
                  .maxAge(4, TimeUnit.HOURS)
                  .cachePrivate())
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

    val pageRequest = PageRequest.of(
        page.pageNumber,
        page.pageSize,
        if (page.sort.isSorted) page.sort
        else Sort.by(Sort.Order.asc("number"))
    )

    return if (readyFilter) {
      bookRepository.findAllByMetadataStatusAndSeriesId(BookMetadata.Status.READY, id, pageRequest)
    } else {
      bookRepository.findAllBySeriesId(id, pageRequest)
    }.map { it.toDto() }
  }
}
