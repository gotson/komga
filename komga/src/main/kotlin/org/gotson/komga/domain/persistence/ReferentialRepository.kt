package org.gotson.komga.domain.persistence

import java.time.LocalDate

interface ReferentialRepository {
  fun findAuthorsByName(search: String): List<String>

  fun findAllGenres(): Set<String>
  fun findAllGenresByLibrary(libraryId: String): Set<String>
  fun findAllGenresByCollection(collectionId: String): Set<String>

  fun findAllTags(): Set<String>
  fun findAllTagsByLibrary(libraryId: String): Set<String>
  fun findAllTagsBySeries(seriesId: String): Set<String>
  fun findAllTagsByCollection(collectionId: String): Set<String>

  fun findAllLanguages(): Set<String>
  fun findAllLanguagesByLibrary(libraryId: String): Set<String>
  fun findAllLanguagesByCollection(collectionId: String): Set<String>

  fun findAllPublishers(): Set<String>
  fun findAllPublishersByLibrary(libraryId: String): Set<String>
  fun findAllPublishersByLibraries(libraryIds: Set<String>): Set<String>
  fun findAllPublishersByCollection(collectionId: String): Set<String>

  fun findAllAgeRatings(): Set<Int?>
  fun findAllAgeRatingsByLibrary(libraryId: String): Set<Int?>
  fun findAllAgeRatingsByCollection(collectionId: String): Set<Int?>

  fun findAllSeriesReleaseDates(): Set<LocalDate>
  fun findAllSeriesReleaseDatesByLibrary(libraryId: String): Set<LocalDate>
  fun findAllSeriesReleaseDatesByCollection(collectionId: String): Set<LocalDate>
}
