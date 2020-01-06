package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.Media
import org.hibernate.annotations.QueryHints.CACHEABLE
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.stereotype.Repository
import java.net.URL
import javax.persistence.QueryHint

@Repository
interface BookRepository : JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
  @QueryHints(QueryHint(name = CACHEABLE, value = "true"))
  override fun findAll(pageable: Pageable): Page<Book>

  @QueryHints(QueryHint(name = CACHEABLE, value = "true"))
  fun findAllBySeriesId(seriesId: Long, pageable: Pageable): Page<Book>

  @QueryHints(QueryHint(name = CACHEABLE, value = "true"))
  fun findAllByMediaStatusInAndSeriesId(status: Collection<Media.Status>, seriesId: Long, pageable: Pageable): Page<Book>

  @QueryHints(QueryHint(name = CACHEABLE, value = "true"))
  fun findBySeriesLibraryIn(seriesLibrary: Collection<Library>, pageable: Pageable): Page<Book>

  fun findBySeriesLibraryIn(seriesLibrary: Collection<Library>): List<Book>

  fun findByUrl(url: URL): Book?
  fun findAllByMediaStatus(status: Media.Status): List<Book>
  fun findAllByMediaThumbnailIsNull(): List<Book>
}
