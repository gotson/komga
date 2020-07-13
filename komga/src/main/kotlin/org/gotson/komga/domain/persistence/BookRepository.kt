package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookSearch

interface BookRepository {
  fun findByIdOrNull(bookId: String): Book?
  fun findBySeriesId(seriesId: Long): Collection<Book>
  fun findAll(): Collection<Book>
  fun findAll(bookSearch: BookSearch): Collection<Book>

  fun getLibraryId(bookId: String): Long?
  fun findFirstIdInSeries(seriesId: Long): String?
  fun findAllIdBySeriesId(seriesId: Long): Collection<String>
  fun findAllIdBySeriesIds(seriesIds: Collection<Long>): Collection<String>
  fun findAllIdByLibraryId(libraryId: Long): Collection<String>
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
