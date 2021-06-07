package org.gotson.komga.infrastructure.metadata.mylar

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.SeriesMetadataPatch
import org.gotson.komga.infrastructure.metadata.SeriesMetadataProvider
import org.gotson.komga.infrastructure.metadata.mylar.dto.Status
import org.springframework.stereotype.Service
import kotlin.io.path.notExists
import org.gotson.komga.infrastructure.metadata.mylar.dto.Series as MylarSeries

private val logger = KotlinLogging.logger {}

private const val SERIES_JSON = "series.json"

@Service
class MylarSeriesProvider(
  private val mapper: ObjectMapper,
) : SeriesMetadataProvider {

  override fun getSeriesMetadata(series: Series): SeriesMetadataPatch? {
    try {
      val seriesJsonPath = series.path.resolve(SERIES_JSON)
      if (seriesJsonPath.notExists()) {
        logger.debug { "Series folder does not contain any $SERIES_JSON file: $series" }
        return null
      }
      val metadata = mapper.readValue(seriesJsonPath.toFile(), MylarSeries::class.java).metadata.first()

      return SeriesMetadataPatch(
        title = metadata.name,
        titleSort = metadata.name,
        status = when (metadata.status) {
          Status.Ended -> SeriesMetadata.Status.ENDED
          Status.Continuing -> SeriesMetadata.Status.ONGOING
        },
        summary = metadata.descriptionFormatted ?: metadata.descriptionText,
        readingDirection = null,
        publisher = metadata.publisher,
        ageRating = null,
        language = null,
        genres = null,
        collections = emptyList(),
      )
    } catch (e: Exception) {
      logger.error(e) { "Error while retrieving metadata from $SERIES_JSON" }
      return null
    }
  }
}
