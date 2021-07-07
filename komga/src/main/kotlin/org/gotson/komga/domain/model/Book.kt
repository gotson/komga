package org.gotson.komga.domain.model

import com.github.f4b6a3.tsid.TsidCreator
import com.jakewharton.byteunits.BinaryByteUnit
import java.io.Serializable
import java.net.URL
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.io.path.toPath

data class Book(
  val name: String,
  val url: URL,
  val fileLastModified: LocalDateTime,
  val fileSize: Long = 0,
  val fileHash: String = "",
  val number: Int = 0,

  val id: String = TsidCreator.getTsid256().toString(),
  val seriesId: String = "",
  val libraryId: String = "",

  val deletedDate: LocalDateTime? = null,

  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = LocalDateTime.now()
) : Auditable(), Serializable {

  @delegate:Transient
  val path: Path by lazy { this.url.toURI().toPath() }

  @delegate:Transient
  val fileSizeHumanReadable: String by lazy { BinaryByteUnit.format(fileSize) }
}
