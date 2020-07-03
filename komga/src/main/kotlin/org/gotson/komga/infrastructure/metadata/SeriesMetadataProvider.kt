package org.gotson.komga.infrastructure.metadata

import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.SeriesMetadataPatch

interface SeriesMetadataProvider {
  fun getSeriesMetadataFromBook(book: Book, media: Media): SeriesMetadataPatch?
}
