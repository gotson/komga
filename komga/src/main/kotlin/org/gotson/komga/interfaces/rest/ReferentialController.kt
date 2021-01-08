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
    @RequestParam(name = "library_id", required = false) libraryId: String?,
    @RequestParam(name = "collection_id", required = false) collectionId: String?
  ): Set<String> =
    when {
      libraryId != null -> referentialRepository.findAllGenresByLibrary(libraryId)
      collectionId != null -> referentialRepository.findAllGenresByCollection(collectionId)
      else -> referentialRepository.findAllGenres()
    }

  @GetMapping("/tags")
  fun getTags(
    @RequestParam(name = "library_id", required = false) libraryId: String?,
    @RequestParam(name = "series_id", required = false) seriesId: String?,
    @RequestParam(name = "collection_id", required = false) collectionId: String?
  ): Set<String> =
    when {
      libraryId != null -> referentialRepository.findAllTagsByLibrary(libraryId)
      seriesId != null -> referentialRepository.findAllTagsBySeries(seriesId)
      collectionId != null -> referentialRepository.findAllTagsByCollection(collectionId)
      else -> referentialRepository.findAllTags()
    }

  @GetMapping("/languages")
  fun getLanguages(
    @RequestParam(name = "library_id", required = false) libraryId: String?,
    @RequestParam(name = "collection_id", required = false) collectionId: String?
  ): Set<String> =
    when {
      libraryId != null -> referentialRepository.findAllLanguagesByLibrary(libraryId)
      collectionId != null -> referentialRepository.findAllLanguagesByCollection(collectionId)
      else -> referentialRepository.findAllLanguages()
    }

  @GetMapping("/publishers")
  fun getPublishers(
    @RequestParam(name = "library_id", required = false) libraryId: String?,
    @RequestParam(name = "collection_id", required = false) collectionId: String?
  ): Set<String> =
    when {
      libraryId != null -> referentialRepository.findAllPublishersByLibrary(libraryId)
      collectionId != null -> referentialRepository.findAllPublishersByCollection(collectionId)
      else -> referentialRepository.findAllPublishers()
    }

  @GetMapping("/age-ratings")
  fun getAgeRatings(
    @RequestParam(name = "library_id", required = false) libraryId: String?,
    @RequestParam(name = "collection_id", required = false) collectionId: String?
  ): Set<String> =
    when {
      libraryId != null -> referentialRepository.findAllAgeRatingsByLibrary(libraryId)
      collectionId != null -> referentialRepository.findAllAgeRatingsByCollection(collectionId)
      else -> referentialRepository.findAllAgeRatings()
    }.map { it?.toString() ?: "None" }.toSet()
}
