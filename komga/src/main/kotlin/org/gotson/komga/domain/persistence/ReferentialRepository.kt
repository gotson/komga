package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.FilterBy
import org.gotson.komga.domain.model.FilterTags
import org.gotson.komga.domain.model.SearchContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate

interface ReferentialRepository {
  @Deprecated("Use findAuthors instead")
  fun findAllAuthorsByName(
    search: String,
    filterOnLibraryIds: Collection<String>?,
  ): List<Author>

  @Deprecated("Use findAuthors instead")
  fun findAllAuthorsByNameAndLibrary(
    search: String,
    libraryId: String,
    filterOnLibraryIds: Collection<String>?,
  ): List<Author>

  @Deprecated("Use findAuthors instead")
  fun findAllAuthorsByNameAndCollection(
    search: String,
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): List<Author>

  @Deprecated("Use findAuthors instead")
  fun findAllAuthorsByNameAndSeries(
    search: String,
    seriesId: String,
    filterOnLibraryIds: Collection<String>?,
  ): List<Author>

  @Deprecated("Use findAuthorsNames instead")
  fun findAllAuthorsNamesByName(
    search: String,
    filterOnLibraryIds: Collection<String>?,
  ): List<String>

  @Deprecated("Use findAuthorsRoles instead")
  fun findAllAuthorsRoles(filterOnLibraryIds: Collection<String>?): List<String>

  fun findAuthors(
    context: SearchContext,
    search: String?,
    role: String?,
    filterBy: FilterBy?,
    pageable: Pageable,
  ): Page<Author>

  fun findAuthorsRoles(
    context: SearchContext,
    filterBy: FilterBy?,
    pageable: Pageable,
  ): Page<String>

  fun findAuthorsNames(
    context: SearchContext,
    search: String?,
    role: String?,
    filterBy: FilterBy?,
    pageable: Pageable,
  ): Page<String>

  @Deprecated("Use findGenres instead")
  fun findAllGenres(filterOnLibraryIds: Collection<String>?): Set<String>

  @Deprecated("Use findGenres instead")
  fun findAllGenresByLibraries(
    libraryIds: Set<String>,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>

  @Deprecated("Use findGenres instead")
  fun findAllGenresByCollection(
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>

  fun findGenres(
    context: SearchContext,
    search: String?,
    filterBy: FilterBy?,
    pageable: Pageable,
  ): Page<String>

  @Deprecated("Use findTags instead")
  fun findAllSeriesAndBookTags(filterOnLibraryIds: Collection<String>?): Set<String>

  @Deprecated("Use findTags instead")
  fun findAllSeriesAndBookTagsByLibraries(
    libraryIds: Set<String>,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>

  @Deprecated("Use findTags instead")
  fun findAllSeriesAndBookTagsByCollection(
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>

  @Deprecated("Use findTags instead")
  fun findAllSeriesTags(filterOnLibraryIds: Collection<String>?): Set<String>

  @Deprecated("Use findTags instead")
  fun findAllSeriesTagsByLibrary(
    libraryId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>

  @Deprecated("Use findTags instead")
  fun findAllSeriesTagsByCollection(
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>

  @Deprecated("Use findTags instead")
  fun findAllBookTags(filterOnLibraryIds: Collection<String>?): Set<String>

  @Deprecated("Use findTags instead")
  fun findAllBookTagsBySeries(
    seriesId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>

  @Deprecated("Use findTags instead")
  fun findAllBookTagsByReadList(
    readListId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>

  fun findTags(
    context: SearchContext,
    search: String?,
    filterBy: FilterBy?,
    filterTags: FilterTags,
    pageable: Pageable,
  ): Page<String>

  @Deprecated("Use findLanguages instead")
  fun findAllLanguages(filterOnLibraryIds: Collection<String>?): Set<String>

  @Deprecated("Use findLanguages instead")
  fun findAllLanguagesByLibraries(
    libraryIds: Set<String>,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>

  @Deprecated("Use findLanguages instead")
  fun findAllLanguagesByCollection(
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>

  fun findLanguages(
    context: SearchContext,
    search: String?,
    filterBy: FilterBy?,
    pageable: Pageable,
  ): Page<String>

  @Deprecated("Use findPublishers instead")
  fun findAllPublishers(filterOnLibraryIds: Collection<String>?): Set<String>

  @Deprecated("Use findPublishers instead")
  fun findAllPublishers(
    filterOnLibraryIds: Collection<String>?,
    pageable: Pageable,
  ): Page<String>

  @Deprecated("Use findPublishers instead")
  fun findAllPublishersByLibraries(
    libraryIds: Set<String>,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>

  @Deprecated("Use findPublishers instead")
  fun findAllPublishersByCollection(
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>

  fun findPublishers(
    context: SearchContext,
    search: String?,
    filterBy: FilterBy?,
    pageable: Pageable,
  ): Page<String>

  @Deprecated("Use findAgeRatings instead")
  fun findAllAgeRatings(filterOnLibraryIds: Collection<String>?): Set<Int?>

  @Deprecated("Use findAgeRatings instead")
  fun findAllAgeRatingsByLibraries(
    libraryIds: Set<String>,
    filterOnLibraryIds: Collection<String>?,
  ): Set<Int?>

  @Deprecated("Use findAgeRatings instead")
  fun findAllAgeRatingsByCollection(
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<Int?>

  fun findAgeRatings(
    context: SearchContext,
    filterBy: FilterBy?,
    pageable: Pageable,
  ): Page<Int>

  @Deprecated("Use findSeriesReleaseDates instead")
  fun findAllSeriesReleaseDates(filterOnLibraryIds: Collection<String>?): Set<LocalDate>

  @Deprecated("Use findSeriesReleaseDates instead")
  fun findAllSeriesReleaseDatesByLibraries(
    libraryIds: Set<String>,
    filterOnLibraryIds: Collection<String>?,
  ): Set<LocalDate>

  @Deprecated("Use findSeriesReleaseDates instead")
  fun findAllSeriesReleaseDatesByCollection(
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<LocalDate>

  fun findSeriesReleaseDates(
    context: SearchContext,
    filterBy: FilterBy?,
    pageable: Pageable,
  ): Page<String>

  @Deprecated("Use findSharingLabels instead")
  fun findAllSharingLabels(filterOnLibraryIds: Collection<String>?): Set<String>

  @Deprecated("Use findSharingLabels instead")
  fun findAllSharingLabelsByLibraries(
    libraryIds: Set<String>,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>

  @Deprecated("Use findSharingLabels instead")
  fun findAllSharingLabelsByCollection(
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String>

  fun findSharingLabels(
    context: SearchContext,
    search: String?,
    filterBy: FilterBy?,
    pageable: Pageable,
  ): Page<String>
}
