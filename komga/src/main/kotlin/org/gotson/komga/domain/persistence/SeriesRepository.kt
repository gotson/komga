package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.Series
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.net.URL

@Repository
interface SeriesRepository : JpaRepository<Series, Long>, JpaSpecificationExecutor<Series> {
  fun findByLibraryIn(libraries: Collection<Library>, sort: Sort): List<Series>
  fun findByLibraryIn(libraries: Collection<Library>, page: Pageable): Page<Series>
  fun findByLibraryId(libraryId: Long, sort: Sort): List<Series>
  fun findByLibraryIdAndUrlNotIn(libraryId: Long, urls: Collection<URL>): List<Series>
  fun findByLibraryIdAndUrl(libraryId: Long, url: URL): Series?
  fun deleteByLibraryId(libraryId: Long)
}
