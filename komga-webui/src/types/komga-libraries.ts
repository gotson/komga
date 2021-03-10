interface LibraryCreationDto {
  name: string,
  root: string,
  importComicInfoBook: boolean,
  importComicInfoSeries: boolean,
  importComicInfoCollection: boolean,
  importComicInfoReadList: boolean,
  importEpubBook: boolean,
  importEpubSeries: boolean,
  importLocalArtwork: boolean,
  importBarcodeIsbn: boolean,
  scanForceModifiedTime: boolean,
  scanDeep: boolean
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
  importLocalArtwork: boolean,
  importBarcodeIsbn: boolean,
  scanForceModifiedTime: boolean,
  scanDeep: boolean
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
  importLocalArtwork: boolean,
  importBarcodeIsbn: boolean,
  scanForceModifiedTime: boolean,
  scanDeep: boolean
}
