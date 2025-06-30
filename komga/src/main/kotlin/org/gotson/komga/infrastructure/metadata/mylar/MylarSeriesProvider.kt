package org.gotson.komga.infrastructure.metadata.mylar

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.MetadataPatchTarget
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.SeriesMetadataPatch
import org.gotson.komga.domain.model.Sidecar
import org.gotson.komga.infrastructure.metadata.SeriesMetadataProvider
import org.gotson.komga.infrastructure.metadata.mylar.dto.Status
import org.gotson.komga.infrastructure.sidecar.SidecarSeriesConsumer
import org.springframework.stereotype.Service
import kotlin.io.path.notExists
import org.gotson.komga.infrastructure.metadata.mylar.dto.Series as MylarSeries

private val logger = KotlinLogging.logger {}

private const val SERIES_JSON = "series.json"

@Service
class MylarSeriesProvider(
  private val mapper: ObjectMapper,
) : SeriesMetadataProvider,
  SidecarSeriesConsumer {
  override fun getSeriesMetadata(series: Series): SeriesMetadataPatch? {
    if (series.oneshot) {
      logger.debug { "Disabled for oneshot series, skipping" }
      return null
    }

    try {
      val seriesJsonPath = series.path.resolve(SERIES_JSON)
      if (seriesJsonPath.notExists()) {
        logger.debug { "Series folder does not contain any $SERIES_JSON file: $series" }
        return null
      }
      val metadata = mapper.readValue(seriesJsonPath.toFile(), MylarSeries::class.java).metadata

      val title =
        if (metadata.volume == null || metadata.volume == 1)
          metadata.name
        else
          "${metadata.name} (${metadata.year})"

      return SeriesMetadataPatch(
        title = title,
        titleSort = title,
        status =
          when (metadata.status) {
            Status.Ended -> SeriesMetadata.Status.ENDED
            Status.Continuing -> SeriesMetadata.Status.ONGOING
          },
        summary = metadata.descriptionFormatted ?: metadata.descriptionText,
        readingDirection = null,
        publisher = metadata.publisher,
        ageRating = metadata.ageRating?.ageRating,
        language = null,
        genres = null,
        totalBookCount = metadata.totalIssues,
        collections = emptySet(),
      )
    } catch (e: Exception) {
      logger.error(e) { "Error while retrieving metadata from $SERIES_JSON" }
      return null
    }
  }

  override fun shouldLibraryHandlePatch(
    library: Library,
    target: MetadataPatchTarget,
  ): Boolean =
    when (target) {
      MetadataPatchTarget.SERIES -> library.importMylarSeries
      else -> false
    }

  override fun getSidecarSeriesType(): Sidecar.Type = Sidecar.Type.METADATA

  override fun getSidecarSeriesFilenames(): List<String> = listOf(SERIES_JSON)
}
