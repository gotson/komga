package org.gotson.komga.infrastructure.metadata

import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.SeriesMetadataPatch

interface SeriesMetadataFromBookProvider : MetadataProvider {
  val supportsAppendVolume: Boolean

  fun getSeriesMetadataFromBook(
    book: BookWithMedia,
    appendVolumeToTitle: Boolean,
  ): SeriesMetadataPatch?
}
