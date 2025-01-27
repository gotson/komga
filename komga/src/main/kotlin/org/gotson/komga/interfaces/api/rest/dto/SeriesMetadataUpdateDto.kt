package org.gotson.komga.interfaces.api.rest.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.infrastructure.validation.NullOrBlankOrBCP47
import org.gotson.komga.infrastructure.validation.NullOrNotBlank
import kotlin.properties.Delegates

class SeriesMetadataUpdateDto {
  private val isSet = mutableMapOf<String, Boolean>()

  fun isSet(prop: String) = isSet.getOrDefault(prop, false)

  val status: SeriesMetadata.Status? = null

  val statusLock: Boolean? = null

  @get:NullOrNotBlank
  val title: String? = null

  val titleLock: Boolean? = null

  @get:NullOrNotBlank
  val titleSort: String? = null

  val titleSortLock: Boolean? = null

  var summary: String? = null

  var summaryLock: Boolean? = null

  var publisher: String? = null

  var publisherLock: Boolean? = null

  var readingDirection: SeriesMetadata.ReadingDirection?
    by Delegates.observable(null) { prop, _, _ ->
      isSet[prop.name] = true
    }

  var readingDirectionLock: Boolean? = null

  @get:PositiveOrZero
  var ageRating: Int?
    by Delegates.observable(null) { prop, _, _ ->
      isSet[prop.name] = true
    }

  var ageRatingLock: Boolean? = null

  @get:NullOrBlankOrBCP47
  var language: String? = null

  var languageLock: Boolean? = null

  var genres: Set<String>?
    by Delegates.observable(null) { prop, _, _ ->
      isSet[prop.name] = true
    }

  var genresLock: Boolean? = null

  var tags: Set<String>?
    by Delegates.observable(null) { prop, _, _ ->
      isSet[prop.name] = true
    }

  var tagsLock: Boolean? = null

  @get:Positive
  var totalBookCount: Int?
    by Delegates.observable(null) { prop, _, _ ->
      isSet[prop.name] = true
    }

  var totalBookCountLock: Boolean? = null

  var sharingLabels: Set<String>?
    by Delegates.observable(null) { prop, _, _ ->
      isSet[prop.name] = true
    }

  var sharingLabelsLock: Boolean? = null

  @get:Valid
  var links: List<WebLinkUpdateDto>?
    by Delegates.observable(null) { prop, _, _ ->
      isSet[prop.name] = true
    }

  var linksLock: Boolean? = null

  @get:Valid
  var alternateTitles: List<AlternateTitleUpdateDto>?
    by Delegates.observable(null) { prop, _, _ ->
      isSet[prop.name] = true
    }

  var alternateTitlesLock: Boolean? = null
}
