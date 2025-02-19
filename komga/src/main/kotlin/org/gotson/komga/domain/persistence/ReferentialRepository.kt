package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.Author
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate

interface ReferentialRepository {
  fun findAllAuthorsByName(
    search: String,
    filterOnLibraryIds: Collection<String>?,
  ): List<Author>

  fun findAllAuthorsByNameAndLibrary(
    search: String,
    libraryId: String,
    filterOnLibraryIds: Collection<String>?,
  ): List<Author>

  fun findAllAuthorsByNameAndCollection(
    search: String,
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): List<Author>

  fun findAllAuthorsByNameAndSeries(
    search: String,
    seriesId: String,
    filterOnLibraryIds: Collection<String>?,
  ): List<Author>

  fun findAllAuthorsNamesByName(
    search: String,
    filterOnLibraryIds: Collection<String>?,
  ): List<String>

  fun findAllAuthorsRoles(filterOnLibraryIds: Collection<String>?): List<String>

  fun findAllAuthorsByName(
    search: String?,
    role: String?,
    filterOnLibraryIds: Collection<String>?,
    pageable: Pageable,
  ): Page<Author>

  fun findAllAuthorsByNameAndLibraries(
    search: String?,
    role: String?,
    libraryIds: Set<String>,
    filterOnLibraryIds: Collection<String>?,
    pageable: Pageable,
  ): Page<Author>

  fun findAllAuthorsByNameAndCollection(
    search: String?,
    role: String?,
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
    pageable: Pageable,
  ): Page<Author>

  fun findAllAuthorsByNameAndSeries(
    search: String?,
    role: String?,
    seriesId: String,
    filterOnLibraryIds: Collection<String>?,
    pageable: Pageable,
  ): Page<Author>

  fun findAllAuthorsByNameAndReadList(
    search: String?,
    role: String?,
    readListId: String,
    filterOnLibraryIds: Collection<String>?,
    pageable: Pageable,
  ): Page<Author>

  fun findAllGenres(filterOnLibraryIds: Collection<String>?): Set<String>

  fun findAllGenresByLibraries(
    libraryIds: Set<String>,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>

  fun findAllGenresByCollection(
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>

  fun findAllSeriesAndBookTags(filterOnLibraryIds: Collection<String>?): Set<String>

  fun findAllSeriesAndBookTagsByLibraries(
    libraryIds: Set<String>,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>

  fun findAllSeriesAndBookTagsByCollection(
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>

  fun findAllSeriesTags(filterOnLibraryIds: Collection<String>?): Set<String>

  fun findAllSeriesTagsByLibrary(
    libraryId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>

  fun findAllSeriesTagsByCollection(
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>

  fun findAllBookTags(filterOnLibraryIds: Collection<String>?): Set<String>

  fun findAllBookTagsBySeries(
    seriesId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>

  fun findAllBookTagsByReadList(
    readListId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>

  fun findAllLanguages(filterOnLibraryIds: Collection<String>?): Set<String>

  fun findAllLanguagesByLibraries(
    libraryIds: Set<String>,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>

  fun findAllLanguagesByCollection(
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>

  fun findAllPublishers(filterOnLibraryIds: Collection<String>?): Set<String>

  fun findAllPublishers(
    filterOnLibraryIds: Collection<String>?,
    pageable: Pageable,
  ): Page<String>

  fun findAllPublishersByLibraries(
    libraryIds: Set<String>,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>

  fun findAllPublishersByCollection(
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>

  fun findAllAgeRatings(filterOnLibraryIds: Collection<String>?): Set<Int?>

  fun findAllAgeRatingsByLibraries(
    libraryIds: Set<String>,
    filterOnLibraryIds: Collection<String>?,
  ): Set<Int?>

  fun findAllAgeRatingsByCollection(
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<Int?>

  fun findAllSeriesReleaseDates(filterOnLibraryIds: Collection<String>?): Set<LocalDate>

  fun findAllSeriesReleaseDatesByLibraries(
    libraryIds: Set<String>,
    filterOnLibraryIds: Collection<String>?,
  ): Set<LocalDate>

  fun findAllSeriesReleaseDatesByCollection(
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<LocalDate>

  fun findAllSharingLabels(filterOnLibraryIds: Collection<String>?): Set<String>

  fun findAllSharingLabelsByLibraries(
    libraryIds: Set<String>,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>

  fun findAllSharingLabelsByCollection(
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>
}
