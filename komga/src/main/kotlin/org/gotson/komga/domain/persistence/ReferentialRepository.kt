package org.gotson.komga.domain.persistence

interface ReferentialRepository {
  fun findAuthorsByName(search: String): List<String>

  fun findAllGenres(): Set<String>
  fun findAllGenresByLibrary(libraryId: String): Set<String>

  fun findAllTags(): Set<String>
  fun findAllTagsByLibrary(libraryId: String): Set<String>
  fun findAllTagsBySeries(seriesId: String): Set<String>

  fun findAllLanguages(): Set<String>
  fun findAllLanguagesByLibrary(libraryId: String): Set<String>

  fun findAllPublishers(): Set<String>
  fun findAllPublishersByLibrary(libraryId: String): Set<String>
}
