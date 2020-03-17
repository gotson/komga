package org.gotson.komga.domain.model

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.time.LocalDate
import javax.persistence.Cacheable
import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.Table
import javax.validation.constraints.NotBlank
import javax.validation.constraints.PositiveOrZero

@Entity
@Table(name = "book_metadata")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "cache.book_metadata")
class BookMetadata : AuditableEntity {
  constructor(
    title: String,
    summary: String = "",
    number: String,
    numberSort: Float,
    readingDirection: ReadingDirection? = null,
    publisher: String = "",
    ageRating: Int? = null,
    releaseDate: LocalDate? = null,
    authors: MutableList<Author> = mutableListOf()
  ) : super() {
    this.title = title
    this.summary = summary
    this.number = number
    this.numberSort = numberSort
    this.readingDirection = readingDirection
    this.publisher = publisher
    this.ageRating = ageRating
    this.releaseDate = releaseDate
    this.authors = authors
  }

  @Id
  @GeneratedValue
  @Column(name = "id", nullable = false, unique = true)
  val id: Long = 0

  @NotBlank
  @Column(name = "title", nullable = false)
  var title: String
    set(value) {
      require(value.isNotBlank()) { "title must not be blank" }
      field = value.trim()
    }

  @Column(name = "summary", nullable = false)
  var summary: String
    set(value) {
      field = value.trim()
    }

  @NotBlank
  @Column(name = "number", nullable = false)
  var number: String
    set(value) {
      require(value.isNotBlank()) { "number must not be blank" }
      field = value.trim()
    }

  @Column(name = "number_sort", nullable = false, columnDefinition = "REAL")
  var numberSort: Float

  @Enumerated(EnumType.STRING)
  @Column(name = "reading_direction", nullable = true)
  var readingDirection: ReadingDirection?

  @Column(name = "publisher", nullable = false)
  var publisher: String
    set(value) {
      field = value.trim()
    }

  @PositiveOrZero
  @Column(name = "age_rating", nullable = true)
  var ageRating: Int?

  @Column(name = "release_date", nullable = true)
  var releaseDate: LocalDate?

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "book_metadata_author", joinColumns = [JoinColumn(name = "book_metadata_id")])
  var authors: MutableList<Author>


  @Column(name = "title_lock", nullable = false)
  var titleLock: Boolean = false

  @Column(name = "summary_lock", nullable = false)
  var summaryLock: Boolean = false

  @Column(name = "number_lock", nullable = false)
  var numberLock: Boolean = false

  @Column(name = "number_sort_lock", nullable = false)
  var numberSortLock: Boolean = false

  @Column(name = "reading_direction_lock", nullable = false)
  var readingDirectionLock: Boolean = false

  @Column(name = "publisher_lock", nullable = false)
  var publisherLock: Boolean = false

  @Column(name = "age_rating_lock", nullable = false)
  var ageRatingLock: Boolean = false

  @Column(name = "release_date_lock", nullable = false)
  var releaseDateLock: Boolean = false

  @Column(name = "authors_lock", nullable = false)
  var authorsLock: Boolean = false

  enum class ReadingDirection {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT,
    VERTICAL,
    WEBTOON
  }
}
