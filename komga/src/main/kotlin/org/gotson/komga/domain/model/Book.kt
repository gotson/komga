package org.gotson.komga.domain.model

import com.github.f4b6a3.tsid.TsidCreator
import com.jakewharton.byteunits.BinaryByteUnit
import org.apache.commons.io.FilenameUtils
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

  val id: String = TsidCreator.getTsidString256(),
  val seriesId: String = "",
  val libraryId: String = "",

  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = LocalDateTime.now()
) : Auditable() {

  fun fileName(): String = FilenameUtils.getName(url.toString())

  fun fileExtension(): String = FilenameUtils.getExtension(url.toString())

  fun path(): Path = Paths.get(this.url.toURI())

  fun fileSizeHumanReadable(): String = BinaryByteUnit.format(fileSize)
}
