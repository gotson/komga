package org.gotson.komga.interfaces.api.rest.dto

import javax.validation.constraints.NotBlank

data class LibraryUpdateDto(
  @get:NotBlank val name: String,
  @get:NotBlank val root: String,
  val importComicInfoBook: Boolean,
  val importComicInfoSeries: Boolean,
  val importComicInfoCollection: Boolean,
  val importComicInfoReadList: Boolean,
  val importEpubBook: Boolean,
  val importEpubSeries: Boolean,
  val importMylarSeries: Boolean,
  val importLocalArtwork: Boolean,
  val importBarcodeIsbn: Boolean,
  val scanForceModifiedTime: Boolean,
  val scanDeep: Boolean,
  val repairExtensions: Boolean,
  val convertToCbz: Boolean,
  val emptyTrashAfterScan: Boolean,
  val seriesCover: SeriesCoverDto,
)
