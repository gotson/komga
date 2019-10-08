package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.Status
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.net.URL

@Repository
interface BookRepository : JpaRepository<Book, Long> {
  @Query(
      value = "select * from Book b where b.series_id = ?1 order by b.index",
      countQuery = "select count(*) from Book where series_id = ?1",
      nativeQuery = true)
  fun findAllBySeriesId(seriesId: Long, pageable: Pageable): Page<Book>

  @Query(
      value = "select * from Book b, Book_Metadata m where b.book_metadata_id = m.id " +
          "and b.series_id = ?2 and m.status = ?1 order by b.index",
      countQuery = "select count(*) from Book b, Book_Metadata m where b.book_metadata_id = m.id and b.series_id = ?2 and m.status = ?1",
      nativeQuery = true)
  fun findAllByMetadataStatusAndSeriesId(status: String, seriesId: Long, pageable: Pageable): Page<Book>

  fun findByUrl(url: URL): Book?
  fun findAllByMetadataStatus(status: Status): List<Book>
  fun findAllByMetadataThumbnailIsNull(): List<Book>
}
