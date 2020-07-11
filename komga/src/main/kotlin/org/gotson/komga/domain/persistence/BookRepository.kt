package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookSearch

interface BookRepository {
  fun findByIdOrNull(bookId: Long): Book?
  fun findBySeriesId(seriesId: Long): Collection<Book>
  fun findAll(): Collection<Book>
  fun findAll(bookSearch: BookSearch): Collection<Book>

  fun getLibraryId(bookId: Long): Long?
  fun findFirstIdInSeries(seriesId: Long): Long?
  fun findAllIdBySeriesId(seriesId: Long): Collection<Long>
  fun findAllIdBySeriesIds(seriesIds: Collection<Long>): Collection<Long>
  fun findAllIdByLibraryId(libraryId: Long): Collection<Long>
  fun findAllId(bookSearch: BookSearch): Collection<Long>

  fun insert(book: Book): Book
  fun insertMany(books: Collection<Book>): Collection<Book>
  fun update(book: Book)
  fun updateMany(books: Collection<Book>)

  fun delete(bookId: Long)
  fun deleteByBookIds(bookIds: Collection<Long>)
  fun deleteAll()

  fun count(): Long
}
