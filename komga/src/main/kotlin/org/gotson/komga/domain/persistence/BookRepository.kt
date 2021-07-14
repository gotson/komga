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

  fun findAll(): Collection<Book>
  fun findAllBySeriesId(seriesId: String): Collection<Book>
  fun findAllBySeriesIds(seriesIds: Collection<String>): Collection<Book>
  fun findAllByLibraryIdAndUrlNotIn(libraryId: String, urls: Collection<URL>): Collection<Book>
  fun findAll(bookSearch: BookSearch): Collection<Book>
  fun findAll(bookSearch: BookSearch, pageable: Pageable): Page<Book>
  fun findAllDeletedByFileSize(fileSize: Long): Collection<Book>

  fun getLibraryIdOrNull(bookId: String): String?
  fun getSeriesIdOrNull(bookId: String): String?
  fun findFirstIdInSeriesOrNull(seriesId: String): String?

  fun findAllIdsBySeriesId(seriesId: String): Collection<String>
  fun findAllIdsBySeriesIds(seriesIds: Collection<String>): Collection<String>
  fun findAllIdsByLibraryId(libraryId: String): Collection<String>
  fun findAllIdsByLibraryIdAndMediaTypes(libraryId: String, mediaTypes: Collection<String>): Collection<String>
  fun findAllIdsByLibraryIdAndMismatchedExtension(libraryId: String, mediaType: String, extension: String): Collection<String>
  fun findAllIdsByLibraryIdAndWithEmptyHash(libraryId: String): Collection<String>
  fun findAllIds(bookSearch: BookSearch, sort: Sort): Collection<String>

  fun insert(book: Book)
  fun insert(books: Collection<Book>)
  fun update(book: Book)
  fun update(books: Collection<Book>)

  fun delete(bookId: String)
  fun delete(bookIds: Collection<String>)
  fun deleteAll()

  fun count(): Long
}
