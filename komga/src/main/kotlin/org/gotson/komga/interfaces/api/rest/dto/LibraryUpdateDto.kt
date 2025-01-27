package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.infrastructure.validation.NullOrNotBlank
import kotlin.properties.Delegates

class LibraryUpdateDto {
  private val isSet = mutableMapOf<String, Boolean>()

  fun isSet(prop: String) = isSet.getOrDefault(prop, false)

  @get:NullOrNotBlank
  val name: String? = null

  @get:NullOrNotBlank
  val root: String? = null

  val importComicInfoBook: Boolean? = null
  val importComicInfoSeries: Boolean? = null
  val importComicInfoCollection: Boolean? = null
  val importComicInfoReadList: Boolean? = null
  val importComicInfoSeriesAppendVolume: Boolean? = null
  val importEpubBook: Boolean? = null
  val importEpubSeries: Boolean? = null
  val importMylarSeries: Boolean? = null
  val importLocalArtwork: Boolean? = null
  val importBarcodeIsbn: Boolean? = null

  val scanForceModifiedTime: Boolean? = null
  val scanInterval: ScanIntervalDto? = null
  val scanOnStartup: Boolean? = null
  val scanCbx: Boolean? = null
  val scanPdf: Boolean? = null
  val scanEpub: Boolean? = null
  var scanDirectoryExclusions: Set<String>?
    by Delegates.observable(null) { prop, _, _ ->
      isSet[prop.name] = true
    }

  val repairExtensions: Boolean? = null
  val convertToCbz: Boolean? = null
  val emptyTrashAfterScan: Boolean? = null
  val seriesCover: SeriesCoverDto? = null
  val hashFiles: Boolean? = null
  val hashPages: Boolean? = null
  val hashKoreader: Boolean? = null
  val analyzeDimensions: Boolean? = null
  var oneshotsDirectory: String?
    by Delegates.observable(null) { prop, _, _ ->
      isSet[prop.name] = true
    }
}
