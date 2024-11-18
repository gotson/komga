package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaExtension

interface MediaRepository {
  fun findById(bookId: String): Media

  fun findByIdOrNull(bookId: String): Media?

  fun findAllBookIdsByLibraryIdAndMediaTypeAndWithMissingPageHash(
    libraryId: String,
    mediaTypes: Collection<String>,
    pageHashing: Int,
  ): Collection<String>

  fun getPagesSizes(bookIds: Collection<String>): Collection<Pair<String, Int>>

  fun findExtensionByIdOrNull(bookId: String): MediaExtension?

  fun insert(media: Media)

  fun insert(medias: Collection<Media>)

  fun update(media: Media)

  fun delete(bookId: String)

  fun delete(bookIds: Collection<String>)

  fun count(): Long
}
