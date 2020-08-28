package org.gotson.komga.interfaces.rest

import org.gotson.komga.domain.persistence.ReferentialRepository
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("api/v1", produces = [MediaType.APPLICATION_JSON_VALUE])
class ReferentialController(
  private val referentialRepository: ReferentialRepository
) {

  @GetMapping("/authors")
  fun getAuthors(
    @RequestParam(name = "search", defaultValue = "") search: String
  ): List<String> =
    referentialRepository.findAuthorsByName(search)

  @GetMapping("/genres")
  fun getGenres(
    @RequestParam(name = "library_id", required = false) libraryId: String?
  ): Set<String> =
    if (libraryId != null) referentialRepository.findAllGenresByLibrary(libraryId)
    else referentialRepository.findAllGenres()

  @GetMapping("/tags")
  fun getTags(
    @RequestParam(name = "library_id", required = false) libraryId: String?,
    @RequestParam(name = "series_id", required = false) seriesId: String?
  ): Set<String> =
    when {
      libraryId != null -> referentialRepository.findAllTagsByLibrary(libraryId)
      seriesId != null -> referentialRepository.findAllTagsBySeries(seriesId)
      else -> referentialRepository.findAllTags()
    }

  @GetMapping("/languages")
  fun getLanguages(
    @RequestParam(name = "library_id", required = false) libraryId: String?
  ): Set<String> =
    if (libraryId != null) referentialRepository.findAllLanguagesByLibrary(libraryId)
    else referentialRepository.findAllLanguages()

  @GetMapping("/publishers")
  fun getPublishers(
    @RequestParam(name = "library_id", required = false) libraryId: String?
  ): Set<String> =
    if (libraryId != null) referentialRepository.findAllPublishersByLibrary(libraryId)
    else referentialRepository.findAllPublishers()

  @GetMapping("/age-ratings")
  fun getAgeRatings(
    @RequestParam(name = "library_id", required = false) libraryId: String?
  ): Set<String> =
    if (libraryId != null) {
      referentialRepository.findAllAgeRatingsByLibrary(libraryId)
    } else {
      referentialRepository.findAllAgeRatings()
    }.map { it?.toString() ?: "None" }.toSet()
}
