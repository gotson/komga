package org.gotson.komga.domain.model

import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToOne
import javax.persistence.PrimaryKeyJoinColumn
import javax.persistence.Table
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Table(name = "book")
class Book(
    @NotBlank
    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "url", nullable = false)
    val url: URL,

    @Column(name = "updated", nullable = false)
    val updated: LocalDateTime
) {
  @Id
  @GeneratedValue
  @Column(name = "id", nullable = false)
  @PrimaryKeyJoinColumn
  var id: Long = 0

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "serie_id", nullable = false)
  lateinit var serie: Serie

  @OneToOne(optional = false, orphanRemoval = true, cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
  @JoinColumn(name = "book_metadata_id", nullable = false)
  var metadata: BookMetadata = BookMetadata().also { it.book = this }
    set(value) {
      value.book = this
      field = value
    }
}

fun Book.path(): Path = Paths.get(this.url.toURI())