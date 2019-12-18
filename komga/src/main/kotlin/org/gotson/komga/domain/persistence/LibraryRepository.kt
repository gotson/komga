package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.Library
import org.hibernate.annotations.QueryHints.CACHEABLE
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.stereotype.Repository
import javax.persistence.QueryHint

@Repository
interface LibraryRepository : JpaRepository<Library, Long> {
  @QueryHints(QueryHint(name = CACHEABLE, value = "true"))
  override fun findAll(sort: Sort): List<Library>

  @QueryHints(QueryHint(name = CACHEABLE, value = "true"))
  override fun findAllById(ids: Iterable<Long>): List<Library>

  fun existsByName(name: String): Boolean
}
