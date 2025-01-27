package org.gotson.komga.domain.model

import org.gotson.komga.language.lowerNotBlank
import java.time.LocalDateTime

class SeriesMetadata(
  val status: Status = Status.ONGOING,
  title: String,
  titleSort: String = title,
  summary: String = "",
  val readingDirection: ReadingDirection? = null,
  publisher: String = "",
  val ageRating: Int? = null,
  language: String = "",
  genres: Set<String> = emptySet(),
  tags: Set<String> = emptySet(),
  val totalBookCount: Int? = null,
  sharingLabels: Set<String> = emptySet(),
  val links: List<WebLink> = emptyList(),
  val alternateTitles: List<AlternateTitle> = emptyList(),
  val statusLock: Boolean = false,
  val titleLock: Boolean = false,
  val titleSortLock: Boolean = false,
  val summaryLock: Boolean = false,
  val readingDirectionLock: Boolean = false,
  val publisherLock: Boolean = false,
  val ageRatingLock: Boolean = false,
  val languageLock: Boolean = false,
  val genresLock: Boolean = false,
  val tagsLock: Boolean = false,
  val totalBookCountLock: Boolean = false,
  val sharingLabelsLock: Boolean = false,
  val linksLock: Boolean = false,
  val alternateTitlesLock: Boolean = false,
  val seriesId: String = "",
  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = createdDate,
) : Auditable {
  val title = title.trim()
  val titleSort = titleSort.trim()
  val summary = summary.trim()
  val publisher = publisher.trim()
  val language = BCP47TagValidator.normalize(language.trim())
  val tags = tags.lowerNotBlank().toSet()
  val genres = genres.lowerNotBlank().toSet()
  val sharingLabels = sharingLabels.lowerNotBlank().toSet()

  fun copy(
    status: Status = this.status,
    title: String = this.title,
    titleSort: String = this.titleSort,
    summary: String = this.summary,
    readingDirection: ReadingDirection? = this.readingDirection,
    publisher: String = this.publisher,
    ageRating: Int? = this.ageRating,
    language: String = this.language,
    genres: Set<String> = this.genres,
    tags: Set<String> = this.tags,
    totalBookCount: Int? = this.totalBookCount,
    sharingLabels: Set<String> = this.sharingLabels,
    links: List<WebLink> = this.links,
    alternateTitles: List<AlternateTitle> = this.alternateTitles,
    statusLock: Boolean = this.statusLock,
    titleLock: Boolean = this.titleLock,
    titleSortLock: Boolean = this.titleSortLock,
    summaryLock: Boolean = this.summaryLock,
    readingDirectionLock: Boolean = this.readingDirectionLock,
    publisherLock: Boolean = this.publisherLock,
    ageRatingLock: Boolean = this.ageRatingLock,
    languageLock: Boolean = this.languageLock,
    genresLock: Boolean = this.genresLock,
    tagsLock: Boolean = this.tagsLock,
    totalBookCountLock: Boolean = this.totalBookCountLock,
    sharingLabelsLock: Boolean = this.sharingLabelsLock,
    linksLock: Boolean = this.linksLock,
    alternateTitlesLock: Boolean = this.alternateTitlesLock,
    seriesId: String = this.seriesId,
    createdDate: LocalDateTime = this.createdDate,
    lastModifiedDate: LocalDateTime = this.lastModifiedDate,
  ) = SeriesMetadata(
    status = status,
    title = title,
    titleSort = titleSort,
    summary = summary,
    readingDirection = readingDirection,
    publisher = publisher,
    ageRating = ageRating,
    language = language,
    genres = genres,
    tags = tags,
    totalBookCount = totalBookCount,
    sharingLabels = sharingLabels,
    links = links,
    alternateTitles = alternateTitles,
    statusLock = statusLock,
    titleLock = titleLock,
    titleSortLock = titleSortLock,
    summaryLock = summaryLock,
    readingDirectionLock = readingDirectionLock,
    publisherLock = publisherLock,
    ageRatingLock = ageRatingLock,
    languageLock = languageLock,
    genresLock = genresLock,
    tagsLock = tagsLock,
    totalBookCountLock = totalBookCountLock,
    sharingLabelsLock = sharingLabelsLock,
    linksLock = linksLock,
    alternateTitlesLock = alternateTitlesLock,
    seriesId = seriesId,
    createdDate = createdDate,
    lastModifiedDate = lastModifiedDate,
  )

  enum class Status {
    ENDED,
    ONGOING,
    ABANDONED,
    HIATUS,
  }

  enum class ReadingDirection {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT,
    VERTICAL,
    WEBTOON,
  }

  override fun toString(): String = "SeriesMetadata(status=$status, readingDirection=$readingDirection, ageRating=$ageRating, totalBookCount=$totalBookCount, links=$links, alternateTitles=$alternateTitles, statusLock=$statusLock, titleLock=$titleLock, titleSortLock=$titleSortLock, summaryLock=$summaryLock, readingDirectionLock=$readingDirectionLock, publisherLock=$publisherLock, ageRatingLock=$ageRatingLock, languageLock=$languageLock, genresLock=$genresLock, tagsLock=$tagsLock, totalBookCountLock=$totalBookCountLock, sharingLabelsLock=$sharingLabelsLock, linksLock=$linksLock, alternateTitlesLock=$alternateTitlesLock, seriesId='$seriesId', createdDate=$createdDate, lastModifiedDate=$lastModifiedDate, title='$title', titleSort='$titleSort', summary='$summary', publisher='$publisher', language='$language', tags=$tags, genres=$genres, sharingLabels=$sharingLabels)"
}
