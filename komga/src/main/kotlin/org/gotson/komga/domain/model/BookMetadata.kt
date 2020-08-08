package org.gotson.komga.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

class BookMetadata(
  title: String,
  summary: String = "",
  number: String,
  val numberSort: Float,
  val readingDirection: ReadingDirection? = null,
  publisher: String = "",
  val ageRating: Int? = null,
  val releaseDate: LocalDate? = null,
  val authors: List<Author> = emptyList(),

  val titleLock: Boolean = false,
  val summaryLock: Boolean = false,
  val numberLock: Boolean = false,
  val numberSortLock: Boolean = false,
  val readingDirectionLock: Boolean = false,
  val publisherLock: Boolean = false,
  val ageRatingLock: Boolean = false,
  val releaseDateLock: Boolean = false,
  val authorsLock: Boolean = false,

  val bookId: String = "",

  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = LocalDateTime.now()
) : Auditable() {

  val title = title.trim()
  val summary = summary.trim()
  val number = number.trim()
  val publisher = publisher.trim()

  fun copy(
    title: String = this.title,
    summary: String = this.summary,
    number: String = this.number,
    numberSort: Float = this.numberSort,
    readingDirection: ReadingDirection? = this.readingDirection,
    publisher: String = this.publisher,
    ageRating: Int? = this.ageRating,
    releaseDate: LocalDate? = this.releaseDate,
    authors: List<Author> = this.authors.toList(),
    titleLock: Boolean = this.titleLock,
    summaryLock: Boolean = this.summaryLock,
    numberLock: Boolean = this.numberLock,
    numberSortLock: Boolean = this.numberSortLock,
    readingDirectionLock: Boolean = this.readingDirectionLock,
    publisherLock: Boolean = this.publisherLock,
    ageRatingLock: Boolean = this.ageRatingLock,
    releaseDateLock: Boolean = this.releaseDateLock,
    authorsLock: Boolean = this.authorsLock,
    bookId: String = this.bookId,
    createdDate: LocalDateTime = this.createdDate,
    lastModifiedDate: LocalDateTime = this.lastModifiedDate
  ) =
    BookMetadata(
      title = title,
      summary = summary,
      number = number,
      numberSort = numberSort,
      readingDirection = readingDirection,
      publisher = publisher,
      ageRating = ageRating,
      releaseDate = releaseDate,
      authors = authors,
      titleLock = titleLock,
      summaryLock = summaryLock,
      numberLock = numberLock,
      numberSortLock = numberSortLock,
      readingDirectionLock = readingDirectionLock,
      publisherLock = publisherLock,
      ageRatingLock = ageRatingLock,
      releaseDateLock = releaseDateLock,
      authorsLock = authorsLock,
      bookId = bookId,
      createdDate = createdDate,
      lastModifiedDate = lastModifiedDate
    )

  enum class ReadingDirection {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT,
    VERTICAL,
    WEBTOON
  }

  override fun toString(): String =
    "BookMetadata(numberSort=$numberSort, readingDirection=$readingDirection, ageRating=$ageRating, releaseDate=$releaseDate, authors=$authors, titleLock=$titleLock, summaryLock=$summaryLock, numberLock=$numberLock, numberSortLock=$numberSortLock, readingDirectionLock=$readingDirectionLock, publisherLock=$publisherLock, ageRatingLock=$ageRatingLock, releaseDateLock=$releaseDateLock, authorsLock=$authorsLock, bookId=$bookId, createdDate=$createdDate, lastModifiedDate=$lastModifiedDate, title='$title', summary='$summary', number='$number', publisher='$publisher')"
}
