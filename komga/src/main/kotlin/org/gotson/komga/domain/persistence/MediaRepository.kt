package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.Media

interface MediaRepository {
  fun findById(bookId: Long): Media

  fun getThumbnail(bookId: Long): ByteArray?

  fun insert(media: Media): Media
  fun update(media: Media)

  fun delete(bookId: Long)
}
