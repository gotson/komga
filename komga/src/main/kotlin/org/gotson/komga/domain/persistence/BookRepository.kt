package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.Library
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.net.URL

@Repository
interface BookRepository : JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
  fun findAllBySeriesId(seriesId: Long, pageable: Pageable): Page<Book>
  fun findAllByMetadataStatusAndSeriesId(status: BookMetadata.Status, seriesId: Long, pageable: Pageable): Page<Book>

  fun findByUrl(url: URL): Book?
  fun findAllByMetadataStatus(status: BookMetadata.Status): List<Book>
  fun findAllByMetadataThumbnailIsNull(): List<Book>
  fun findBySeriesLibraryIn(seriesLibrary: Collection<Library>, pageable: Pageable): Page<Book>
}
