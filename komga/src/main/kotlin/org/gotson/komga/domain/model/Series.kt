package org.gotson.komga.domain.model

import com.github.f4b6a3.tsid.TsidCreator
import java.net.URL
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.io.path.toPath

data class Series(
  val name: String,
  val url: URL,
  val fileLastModified: LocalDateTime,
  val id: String = TsidCreator.getTsid256().toString(),
  val libraryId: String = "",
  val bookCount: Int = 0,
  val deletedDate: LocalDateTime? = null,
  val oneshot: Boolean = false,
  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = createdDate,
) : Auditable {
  @delegate:Transient
  val path: Path by lazy { this.url.toURI().toPath() }
}
