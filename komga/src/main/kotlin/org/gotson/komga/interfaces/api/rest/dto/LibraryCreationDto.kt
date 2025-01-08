package org.gotson.komga.interfaces.api.rest.dto

import jakarta.validation.constraints.NotBlank

data class LibraryCreationDto(
  @get:NotBlank val name: String,
  @get:NotBlank val root: String,
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
  val scanInterval: ScanIntervalDto = ScanIntervalDto.EVERY_6H,
  val scanOnStartup: Boolean = false,
  val scanCbx: Boolean = true,
  val scanPdf: Boolean = true,
  val scanEpub: Boolean = true,
  val scanDirectoryExclusions: Set<String> = emptySet(),
  val repairExtensions: Boolean = false,
  val convertToCbz: Boolean = false,
  val emptyTrashAfterScan: Boolean = false,
  val seriesCover: SeriesCoverDto = SeriesCoverDto.FIRST,
  val hashFiles: Boolean = true,
  val hashPages: Boolean = false,
  val hashKoreader: Boolean = false,
  val analyzeDimensions: Boolean = true,
  val oneshotsDirectory: String? = null,
)
