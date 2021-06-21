package org.gotson.komga.domain.model

import com.github.f4b6a3.tsid.TsidCreator
import java.io.Serializable
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

data class Series(
  val name: String,
  val url: URL,
  val fileLastModified: LocalDateTime,

  val id: String = TsidCreator.getTsid256().toString(),
  val libraryId: String = "",
  val bookCount: Int = 0,

  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = LocalDateTime.now()
) : Auditable(), Serializable {

  @delegate:Transient
  val path: Path by lazy { Paths.get(this.url.toURI()) }
}
