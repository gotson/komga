package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.Media

interface MediaRepository {
  fun findById(bookId: String): Media
  fun getPagesSize(bookId: String): Int
  fun getPagesSizes(bookIds: Collection<String>): Collection<Pair<String, Int>>

  fun insert(media: Media)
  fun insertMany(medias: Collection<Media>)
  fun update(media: Media)

  fun delete(bookId: String)
  fun deleteByBookIds(bookIds: Collection<String>)
}
