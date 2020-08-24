package org.gotson.komga.domain.persistence

interface ReferentialRepository {
  fun findAuthorsByName(search: String): List<String>
  fun findAllGenres(): Set<String>
  fun findAllTags(): Set<String>
  fun findAllLanguages(): Set<String>
}
