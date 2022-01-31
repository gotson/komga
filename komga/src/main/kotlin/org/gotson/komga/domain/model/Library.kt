package org.gotson.komga.domain.model

import com.github.f4b6a3.tsid.TsidCreator
import java.io.Serializable
import java.net.URL
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.io.path.toPath

data class Library(
  val name: String,
  val root: URL,
  val importComicInfoBook: Boolean = true,
  val importComicInfoSeries: Boolean = true,
  val importComicInfoCollection: Boolean = true,
  val importComicInfoReadList: Boolean = true,
  val importEpubBook: Boolean = true,
  val importEpubSeries: Boolean = true,
  val importMylarSeries: Boolean = true,
  val importLocalArtwork: Boolean = true,
  val importBarcodeIsbn: Boolean = true,
  val scanForceModifiedTime: Boolean = false,
  val scanDeep: Boolean = false,
  val repairExtensions: Boolean = false,
  val convertToCbz: Boolean = false,
  val emptyTrashAfterScan: Boolean = false,
  val seriesCover: SeriesCover = SeriesCover.FIRST,
  val hashFiles: Boolean = true,
  val hashPages: Boolean = false,
  val analyzeDimensions: Boolean = true,

  val unavailableDate: LocalDateTime? = null,

  val id: String = TsidCreator.getTsid256().toString(),

  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = createdDate,
) : Auditable, Serializable {

  enum class SeriesCover {
    FIRST,
    FIRST_UNREAD_OR_FIRST,
    FIRST_UNREAD_OR_LAST,
    LAST,
  }

  @delegate:Transient
  val path: Path by lazy { this.root.toURI().toPath() }
}
