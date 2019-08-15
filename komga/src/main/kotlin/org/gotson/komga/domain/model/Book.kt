package org.gotson.komga.domain.model

import java.net.URL
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
data class Book(
    @Id
    @GeneratedValue
    var id: Long? = null,

    @NotBlank
    val name: String,

    val url: URL,
    val updated: LocalDateTime
) {
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  lateinit var serie: Serie
}