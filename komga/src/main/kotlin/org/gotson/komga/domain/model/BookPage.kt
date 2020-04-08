package org.gotson.komga.domain.model

import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class BookPage(
  @Column(name = "file_name", nullable = false)
  val fileName: String,

  @Column(name = "media_type", nullable = false)
  val mediaType: String
)
