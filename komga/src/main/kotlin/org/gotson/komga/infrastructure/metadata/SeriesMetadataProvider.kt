package org.gotson.komga.infrastructure.metadata

import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.SeriesMetadataPatch

interface SeriesMetadataProvider {
  fun getSeriesMetadata(series: Series): SeriesMetadataPatch?
}
