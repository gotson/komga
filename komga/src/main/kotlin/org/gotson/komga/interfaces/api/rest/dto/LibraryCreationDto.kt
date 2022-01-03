package org.gotson.komga.interfaces.api.rest.dto

import javax.validation.constraints.NotBlank

data class LibraryCreationDto(
  @get:NotBlank val name: String,
  @get:NotBlank val root: String,
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
  val seriesCover: SeriesCoverDto = SeriesCoverDto.FIRST,
)
