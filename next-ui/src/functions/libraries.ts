import type { LibraryCreationDto } from '@/generated/openapi'

export function getLibraryDefaults(): LibraryCreationDto {
  return {
    analyzeDimensions: true,
    convertToCbz: false,
    emptyTrashAfterScan: false,
    hashFiles: true,
    hashKoreader: false,
    hashPages: false,
    importBarcodeIsbn: false,
    importComicInfoBook: true,
    importComicInfoCollection: true,
    importComicInfoReadList: true,
    importComicInfoSeries: true,
    importComicInfoSeriesAppendVolume: true,
    importEpubBook: true,
    importEpubSeries: true,
    importLocalArtwork: true,
    importMylarSeries: true,
    name: '',
    oneshotsDirectory: '_oneshots',
    repairExtensions: false,
    root: '',
    scanCbx: true,
    scanDirectoryExclusions: ['#recycle', '@eaDir', '@Recycle'],
    scanEpub: true,
    scanForceModifiedTime: false,
    scanInterval: 'EVERY_6H',
    scanOnStartup: false,
    scanPdf: true,
    seriesCover: 'FIRST',
  }
}
