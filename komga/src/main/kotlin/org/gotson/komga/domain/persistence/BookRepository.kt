package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookSearch
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.math.BigDecimal
import java.net.URL

interface BookRepository {
  fun findByIdOrNull(bookId: String): Book?

  fun findNotDeletedByLibraryIdAndUrlOrNull(
    libraryId: String,
    url: URL,
  ): Book?

  fun findAll(): Collection<Book>

  fun findAllBySeriesId(seriesId: String): Collection<Book>

  fun findAllBySeriesIds(seriesIds: Collection<String>): Collection<Book>

  fun findAllNotDeletedByLibraryIdAndUrlNotIn(
    libraryId: String,
    urls: Collection<URL>,
  ): Collection<Book>

  fun findAll(bookSearch: BookSearch): Collection<Book>

  fun findAll(
    bookSearch: BookSearch,
    pageable: Pageable,
  ): Page<Book>

  fun findAllDeletedByFileSize(fileSize: Long): Collection<Book>

  fun findAllByLibraryIdAndWithEmptyHash(libraryId: String): Collection<Book>

  fun findAllByLibraryIdAndMediaTypes(
    libraryId: String,
    mediaTypes: Collection<String>,
  ): Collection<Book>

  fun findAllByLibraryIdAndMismatchedExtension(
    libraryId: String,
    mediaType: String,
    extension: String,
  ): Collection<Book>

  fun getLibraryIdOrNull(bookId: String): String?

  fun getSeriesIdOrNull(bookId: String): String?

  fun findFirstIdInSeriesOrNull(seriesId: String): String?

  fun findLastIdInSeriesOrNull(seriesId: String): String?

  fun findFirstUnreadIdInSeriesOrNull(
    seriesId: String,
    userId: String,
  ): String?

  fun findAllIdsBySeriesId(seriesId: String): Collection<String>

  fun findAllIdsBySeriesIds(seriesIds: Collection<String>): Collection<String>

  fun findAllIdsByLibraryId(libraryId: String): Collection<String>

  fun findAllIds(
    bookSearch: BookSearch,
    sort: Sort,
  ): Collection<String>

  fun existsById(bookId: String): Boolean

  fun insert(book: Book)

  fun insert(books: Collection<Book>)

  fun update(book: Book)

  fun update(books: Collection<Book>)

  fun delete(bookId: String)

  fun delete(bookIds: Collection<String>)

  fun deleteAll()

  fun count(): Long

  fun countGroupedByLibraryId(): Map<String, Int>

  fun getFilesizeGroupedByLibraryId(): Map<String, BigDecimal>
}
