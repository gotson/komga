package org.gotson.komga.infrastructure.metadata

import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.SeriesMetadataPatch

interface SeriesMetadataFromBookProvider {
  fun getSeriesMetadataFromBook(book: BookWithMedia): SeriesMetadataPatch?
}
