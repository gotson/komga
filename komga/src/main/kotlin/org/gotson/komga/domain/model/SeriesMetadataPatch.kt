package org.gotson.komga.domain.model

data class SeriesMetadataPatch(
  val title: String?,
  val titles: Map<String, String>? = null, // Language code -> title
  val titleSort: String?,
  val status: SeriesMetadata.Status?,
  val summary: String?,
  val summaries: Map<String, String>? = null, // Language code -> summary
  val readingDirection: SeriesMetadata.ReadingDirection?,
  val publisher: String?,
  val ageRating: Int?,
  val language: String?,
  val genres: Set<String>?,
  val totalBookCount: Int?,
  val collections: Set<String>,
  val alternateTitles: List<AlternateTitle>?,
)
