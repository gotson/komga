package org.gotson.komga.domain.model

import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToOne
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class Book(
    @NotBlank
    val name: String,

    val url: URL,
    val updated: LocalDateTime
) {
  @Id
  @GeneratedValue
  var id: Long = 0

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  lateinit var serie: Serie

  @OneToOne(optional = false, orphanRemoval = true, cascade = [CascadeType.ALL], fetch = FetchType.LAZY, mappedBy = "book")
  var metadata: BookMetadata = BookMetadata().also { it.book = this }
    set(value) {
      value.book = this
      field = value
    }
}

fun Book.path(): Path = Paths.get(this.url.toURI())