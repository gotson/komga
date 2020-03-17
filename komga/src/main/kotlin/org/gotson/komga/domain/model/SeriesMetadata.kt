package org.gotson.komga.domain.model

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import javax.persistence.Cacheable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "series_metadata")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "cache.series_metadata")
class SeriesMetadata : AuditableEntity {
  constructor(
    status: Status = Status.ONGOING,
    title: String,
    titleSort: String = title
  ) : super() {
    this.status = status
    this.title = title
    this.titleSort = titleSort
  }

  @Id
  @GeneratedValue
  @Column(name = "id", nullable = false, unique = true)
  val id: Long = 0


  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  var status: Status

  @NotBlank
  @Column(name = "title", nullable = false)
  var title: String
    set(value) {
      require(value.isNotBlank()) { "title must not be blank" }
      field = value.trim()
    }

  @NotBlank
  @Column(name = "title_sort", nullable = false)
  var titleSort: String
    set(value) {
      require(value.isNotBlank()) { "titleSort must not be blank" }
      field = value.trim()
    }


  @Column(name = "status_lock", nullable = false)
  var statusLock: Boolean = false

  @Column(name = "title_lock", nullable = false)
  var titleLock: Boolean = false

  @Column(name = "title_sort_lock", nullable = false)
  var titleSortLock: Boolean = false


  enum class Status {
    ENDED, ONGOING, ABANDONED, HIATUS
  }
}
