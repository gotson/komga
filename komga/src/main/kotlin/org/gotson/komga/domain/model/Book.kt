package org.gotson.komga.domain.model

import com.github.f4b6a3.tsid.TsidCreator
import com.jakewharton.byteunits.BinaryByteUnit
import java.io.Serializable
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

data class Book(
  val name: String,
  val url: URL,
  val fileLastModified: LocalDateTime,
  val fileSize: Long = 0,
  val number: Int = 0,

  val id: String = TsidCreator.getTsid256().toString(),
  val seriesId: String = "",
  val libraryId: String = "",

  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = LocalDateTime.now()
) : Auditable(), Serializable {

  @delegate:Transient
  val path: Path by lazy { Paths.get(this.url.toURI()) }

  @delegate:Transient
  val fileSizeHumanReadable: String by lazy { BinaryByteUnit.format(fileSize) }
}
