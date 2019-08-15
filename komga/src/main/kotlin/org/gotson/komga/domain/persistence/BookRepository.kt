package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.Book
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.net.URL

@Repository
interface BookRepository : JpaRepository<Book, Long> {
  fun findAllBySerieId(serieId: Long, pageable: Pageable): Page<Book>
  fun findByUrl(url: URL): Book?
}