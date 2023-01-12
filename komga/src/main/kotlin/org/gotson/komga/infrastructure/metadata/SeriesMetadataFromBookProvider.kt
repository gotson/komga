package org.gotson.komga.infrastructure.metadata

import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.SeriesMetadataPatch

interface SeriesMetadataFromBookProvider : MetadataProvider {
  fun getSeriesMetadataFromBook(book: BookWithMedia, library: Library): SeriesMetadataPatch?
}
