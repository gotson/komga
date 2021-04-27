package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.Author
import java.time.LocalDate

interface ReferentialRepository {
  fun findAuthorsByName(search: String, filterOnLibraryIds: Collection<String>?): List<Author>
  fun findAuthorsByNameAndLibrary(search: String, libraryId: String, filterOnLibraryIds: Collection<String>?): List<Author>
  fun findAuthorsByNameAndCollection(search: String, collectionId: String, filterOnLibraryIds: Collection<String>?): List<Author>
  fun findAuthorsByNameAndSeries(search: String, seriesId: String, filterOnLibraryIds: Collection<String>?): List<Author>
  fun findAuthorsNamesByName(search: String, filterOnLibraryIds: Collection<String>?): List<String>
  fun findAuthorsRoles(filterOnLibraryIds: Collection<String>?): List<String>

  fun findAllGenres(filterOnLibraryIds: Collection<String>?): Set<String>
  fun findAllGenresByLibrary(libraryId: String, filterOnLibraryIds: Collection<String>?): Set<String>
  fun findAllGenresByCollection(collectionId: String, filterOnLibraryIds: Collection<String>?): Set<String>

  fun findAllSeriesAndBookTags(filterOnLibraryIds: Collection<String>?): Set<String>
  fun findAllSeriesTags(filterOnLibraryIds: Collection<String>?): Set<String>
  fun findAllSeriesTagsByLibrary(libraryId: String, filterOnLibraryIds: Collection<String>?): Set<String>
  fun findAllSeriesTagsByCollection(collectionId: String, filterOnLibraryIds: Collection<String>?): Set<String>
  fun findAllBookTags(filterOnLibraryIds: Collection<String>?): Set<String>
  fun findAllBookTagsBySeries(seriesId: String, filterOnLibraryIds: Collection<String>?): Set<String>

  fun findAllLanguages(filterOnLibraryIds: Collection<String>?): Set<String>
  fun findAllLanguagesByLibrary(libraryId: String, filterOnLibraryIds: Collection<String>?): Set<String>
  fun findAllLanguagesByCollection(collectionId: String, filterOnLibraryIds: Collection<String>?): Set<String>

  fun findAllPublishers(filterOnLibraryIds: Collection<String>?): Set<String>
  fun findAllPublishersByLibrary(libraryId: String, filterOnLibraryIds: Collection<String>?): Set<String>
  fun findAllPublishersByCollection(collectionId: String, filterOnLibraryIds: Collection<String>?): Set<String>

  fun findAllAgeRatings(filterOnLibraryIds: Collection<String>?): Set<Int?>
  fun findAllAgeRatingsByLibrary(libraryId: String, filterOnLibraryIds: Collection<String>?): Set<Int?>
  fun findAllAgeRatingsByCollection(collectionId: String, filterOnLibraryIds: Collection<String>?): Set<Int?>

  fun findAllSeriesReleaseDates(filterOnLibraryIds: Collection<String>?): Set<LocalDate>
  fun findAllSeriesReleaseDatesByLibrary(libraryId: String, filterOnLibraryIds: Collection<String>?): Set<LocalDate>
  fun findAllSeriesReleaseDatesByCollection(collectionId: String, filterOnLibraryIds: Collection<String>?): Set<LocalDate>
}
