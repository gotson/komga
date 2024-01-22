package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.ThumbnailReadList
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ThumbnailReadListRepository {
  fun findByIdOrNull(thumbnailId: String): ThumbnailReadList?

  fun findSelectedByReadListIdOrNull(readListId: String): ThumbnailReadList?

  fun findAllByReadListId(readListId: String): Collection<ThumbnailReadList>

  fun findAllWithoutMetadata(pageable: Pageable): Page<ThumbnailReadList>

  fun insert(thumbnail: ThumbnailReadList)

  fun update(thumbnail: ThumbnailReadList)

  fun updateMetadata(thumbnails: Collection<ThumbnailReadList>)

  fun markSelected(thumbnail: ThumbnailReadList)

  fun delete(thumbnailReadListId: String)

  fun deleteByReadListId(readListId: String)

  fun deleteByReadListIds(readListIds: Collection<String>)
}
