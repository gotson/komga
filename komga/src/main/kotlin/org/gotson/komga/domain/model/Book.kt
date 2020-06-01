package org.gotson.komga.domain.model

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

  val id: Long = 0,
  val seriesId: Long = 0,
  val libraryId: Long = 0,

  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = LocalDateTime.now()
) : Auditable() {

  fun fileName(): String = FilenameUtils.getName(url.toString())

  fun fileExtension(): String = FilenameUtils.getExtension(url.toString())

  fun path(): Path = Paths.get(this.url.toURI())

  fun fileSizeHumanReadable(): String = BinaryByteUnit.format(fileSize)
}
