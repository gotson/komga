interface LibraryCreationDto {
  name: string,
  root: string,
  importComicInfoBook: boolean,
  importComicInfoSeries: boolean,
  importComicInfoCollection: boolean,
  importComicInfoReadList: boolean,
  importEpubBook: boolean,
  importEpubSeries: boolean,
  importMylarSeries: boolean,
  importLocalArtwork: boolean,
  importBarcodeIsbn: boolean,
  scanForceModifiedTime: boolean,
  scanDeep: boolean,
  repairExtensions: boolean,
  convertToCbz: boolean
}

interface LibraryUpdateDto {
  name: string,
  root: string,
  importComicInfoBook: boolean,
  importComicInfoSeries: boolean,
  importComicInfoCollection: boolean,
  importComicInfoReadList: boolean,
  importEpubBook: boolean,
  importEpubSeries: boolean,
  importMylarSeries: boolean,
  importLocalArtwork: boolean,
  importBarcodeIsbn: boolean,
  scanForceModifiedTime: boolean,
  scanDeep: boolean,
  repairExtensions: boolean,
  convertToCbz: boolean
}

interface LibraryDto {
  id: string,
  name: string,
  root: string,
  importComicInfoBook: boolean,
  importComicInfoSeries: boolean,
  importComicInfoCollection: boolean,
  importComicInfoReadList: boolean,
  importEpubBook: boolean,
  importEpubSeries: boolean,
  importMylarSeries: boolean,
  importLocalArtwork: boolean,
  importBarcodeIsbn: boolean,
  scanForceModifiedTime: boolean,
  scanDeep: boolean,
  repairExtensions: boolean,
  convertToCbz: boolean
}
