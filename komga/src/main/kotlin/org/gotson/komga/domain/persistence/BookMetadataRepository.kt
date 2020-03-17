package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.BookMetadata
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface BookMetadataRepository : JpaRepository<BookMetadata, Long> {
  @Query(
    value = "select distinct a.name from BOOK_METADATA_AUTHOR a where a.name ilike CONCAT('%', :search, '%') order by a.name",
    nativeQuery = true
  )
  fun findAuthorsByName(@Param("search") search: String): List<String>
}
