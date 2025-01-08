package org.gotson.komga.domain.model

import com.github.f4b6a3.tsid.TsidCreator
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
  val importComicInfoSeriesAppendVolume: Boolean = true,
  val importEpubBook: Boolean = true,
  val importEpubSeries: Boolean = true,
  val importMylarSeries: Boolean = true,
  val importLocalArtwork: Boolean = true,
  val importBarcodeIsbn: Boolean = true,
  val scanForceModifiedTime: Boolean = false,
  val scanOnStartup: Boolean = false,
  val scanInterval: ScanInterval = ScanInterval.EVERY_6H,
  val scanCbx: Boolean = true,
  val scanPdf: Boolean = true,
  val scanEpub: Boolean = true,
  val scanDirectoryExclusions: Set<String> = emptySet(),
  val repairExtensions: Boolean = false,
  val convertToCbz: Boolean = false,
  val emptyTrashAfterScan: Boolean = false,
  val seriesCover: SeriesCover = SeriesCover.FIRST,
  val hashFiles: Boolean = true,
  val hashPages: Boolean = false,
  val hashKoreader: Boolean = false,
  val analyzeDimensions: Boolean = true,
  val oneshotsDirectory: String? = null,
  val unavailableDate: LocalDateTime? = null,
  val id: String = TsidCreator.getTsid256().toString(),
  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = createdDate,
) : Auditable {
  enum class SeriesCover {
    FIRST,
    FIRST_UNREAD_OR_FIRST,
    FIRST_UNREAD_OR_LAST,
    LAST,
  }

  enum class ScanInterval {
    DISABLED,
    HOURLY,
    EVERY_6H,
    EVERY_12H,
    DAILY,
    WEEKLY,
  }

  @delegate:Transient
  val path: Path by lazy { this.root.toURI().toPath() }
}
