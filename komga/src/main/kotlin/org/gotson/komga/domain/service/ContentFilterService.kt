package org.gotson.komga.domain.service

import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.language.lowerNotBlank
import org.springframework.stereotype.Service

/**
 * Service for filtering content based on user preferences and restrictions.
 * Includes tag and genre blacklisting.
 */
@Service
class ContentFilterService(
  private val seriesMetadataRepository: SeriesMetadataRepository,
) {
  /**
   * Check if a user can view content with the given tags and genres.
   */
  fun isContentAllowed(
    user: KomgaUser,
    tags: Set<String>,
    genres: Set<String>,
  ): Boolean {
    if (!user.restrictions.isRestricted) {
      return true
    }

    // Check tag blacklist
    if (user.restrictions.tagsExclude.isNotEmpty()) {
      val normalizedTags = tags.lowerNotBlank().toSet()
      if (normalizedTags.intersect(user.restrictions.tagsExclude).isNotEmpty()) {
        return false
      }
    }

    // Check genre blacklist
    if (user.restrictions.genresExclude.isNotEmpty()) {
      val normalizedGenres = genres.lowerNotBlank().toSet()
      if (normalizedGenres.intersect(user.restrictions.genresExclude).isNotEmpty()) {
        return false
      }
    }

    return true
  }

  /**
   * Check if a user can view a specific series based on tags and genres.
   */
  fun isSeriesAllowed(user: KomgaUser, series: Series): Boolean {
    if (!user.restrictions.isRestricted) {
      return true
    }

    val metadata = seriesMetadataRepository.findById(series.id)
    return isContentAllowed(user, metadata.tags, metadata.genres)
  }

  /**
   * Filter a list of series based on user's tag and genre blacklist.
   */
  fun filterSeries(user: KomgaUser, series: List<Series>): List<Series> {
    if (!user.restrictions.isRestricted ||
      (user.restrictions.tagsExclude.isEmpty() && user.restrictions.genresExclude.isEmpty())
    ) {
      return series
    }

    return series.filter { isSeriesAllowed(user, it) }
  }

  /**
   * Filter series metadata based on user's tag and genre blacklist.
   */
  fun filterMetadata(user: KomgaUser, metadataList: List<SeriesMetadata>): List<SeriesMetadata> {
    if (!user.restrictions.isRestricted ||
      (user.restrictions.tagsExclude.isEmpty() && user.restrictions.genresExclude.isEmpty())
    ) {
      return metadataList
    }

    return metadataList.filter { metadata ->
      isContentAllowed(user, metadata.tags, metadata.genres)
    }
  }

  /**
   * Get a summary of what content is being filtered for the user.
   */
  fun getFilterSummary(user: KomgaUser): FilterSummary {
    return FilterSummary(
      hasRestrictions = user.restrictions.isRestricted,
      tagsBlacklisted = user.restrictions.tagsExclude.size,
      genresBlacklisted = user.restrictions.genresExclude.size,
      ageRestricted = user.restrictions.ageRestriction != null,
      labelsRestricted = user.restrictions.labelsAllow.isNotEmpty() || user.restrictions.labelsExclude.isNotEmpty(),
    )
  }

  data class FilterSummary(
    val hasRestrictions: Boolean,
    val tagsBlacklisted: Int,
    val genresBlacklisted: Int,
    val ageRestricted: Boolean,
    val labelsRestricted: Boolean,
  )
}
