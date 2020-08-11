package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.ThumbnailBook

interface ThumbnailBookRepository {
  fun findByBookId(bookId: String): Collection<ThumbnailBook>
  fun findByBookIdAndType(bookId: String, type: ThumbnailBook.Type): Collection<ThumbnailBook>
  fun findSelectedByBookId(bookId: String): ThumbnailBook?

  fun insert(thumbnail: ThumbnailBook)
  fun update(thumbnail: ThumbnailBook)
  fun markSelected(thumbnail: ThumbnailBook)

  fun delete(thumbnailBookId: String)
  fun deleteByBookId(bookId: String)
  fun deleteByBookIdAndType(bookId: String, type: ThumbnailBook.Type)
  fun deleteByBookIds(bookIds: Collection<String>)
}
