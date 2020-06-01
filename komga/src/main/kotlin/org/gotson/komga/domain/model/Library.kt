package org.gotson.komga.domain.model

import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

data class Library(
  val name: String,
  val root: URL,
  val id: Long = 0,
  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = LocalDateTime.now()
) : Auditable() {

  constructor(name: String, root: String) : this(name, Paths.get(root).toUri().toURL())

  fun path(): Path = Paths.get(this.root.toURI())
}
