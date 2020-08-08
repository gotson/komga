package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookSearch

interface BookRepository {
  fun findByIdOrNull(bookId: String): Book?
  fun findBySeriesId(seriesId: String): Collection<Book>
  fun findAll(): Collection<Book>
  fun findAll(bookSearch: BookSearch): Collection<Book>

  fun getLibraryId(bookId: String): String?
  fun findFirstIdInSeries(seriesId: String): String?
  fun findAllIdBySeriesId(seriesId: String): Collection<String>
  fun findAllIdBySeriesIds(seriesIds: Collection<String>): Collection<String>
  fun findAllIdByLibraryId(libraryId: String): Collection<String>
  fun findAllId(bookSearch: BookSearch): Collection<String>

  fun insert(book: Book)
  fun insertMany(books: Collection<Book>)
  fun update(book: Book)
  fun updateMany(books: Collection<Book>)

  fun delete(bookId: String)
  fun deleteByBookIds(bookIds: Collection<String>)
  fun deleteAll()

  fun count(): Long
}
