package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookSearch
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.net.URL

interface BookRepository {
  fun findByIdOrNull(bookId: String): Book?
  fun findByLibraryIdAndUrlOrNull(libraryId: String, url: URL): Book?

  fun findBySeriesId(seriesId: String): Collection<Book>
  fun findAll(): Collection<Book>
  fun findAll(bookSearch: BookSearch): Collection<Book>
  fun findAll(bookSearch: BookSearch, pageable: Pageable): Page<Book>

  fun getLibraryId(bookId: String): String?
  fun findFirstIdInSeries(seriesId: String): String?

  fun findAllIdBySeriesId(seriesId: String): Collection<String>
  fun findAllIdBySeriesIds(seriesIds: Collection<String>): Collection<String>
  fun findAllIdByLibraryId(libraryId: String): Collection<String>
  fun findAllId(bookSearch: BookSearch, sort: Sort): Collection<String>
  fun findAllIdByLibraryIdAndMediaTypes(libraryId: String, mediaTypes: Collection<String>): Collection<String>
  fun findAllIdByLibraryIdAndMismatchedExtension(libraryId: String, mediaType: String, extension: String): Collection<String>

  fun insert(book: Book)
  fun insertMany(books: Collection<Book>)
  fun update(book: Book)
  fun updateMany(books: Collection<Book>)

  fun delete(bookId: String)
  fun deleteByBookIds(bookIds: Collection<String>)
  fun deleteAll()

  fun count(): Long
}
