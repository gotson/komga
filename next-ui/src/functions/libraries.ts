import type { LibraryCreationDto, LibraryDto } from '@/generated/openapi'
import { ClientSettingUser, type ClientSettingUserSettings } from '@/types/ClientSettingsUser'

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

export function getUserLibrariesState(
  libraries: LibraryDto[] | undefined,
  userSettings: ClientSettingUserSettings | undefined,
) {
  const userLibraries = userSettings?.[ClientSettingUser.NextUILibraries] ?? {}

  const ordered =
    libraries?.toSorted(
      (a, b) => (userLibraries?.[a.id]?.order || 0) - (userLibraries?.[b.id]?.order || 0),
    ) ?? []
  console.log('settings:', userLibraries)
  console.log('ordered:', ordered)
  const pinned = ordered.filter((it) => !userLibraries?.[it.id]?.unpinned) || []
  console.log('pinned:', pinned)
  const unpinned = ordered.filter((it) => userLibraries?.[it.id]?.unpinned) || []
  console.log('unpinned:', unpinned)
  const anyPinned = pinned.length > 0
  const anyUnpinned = unpinned.length > 0
  const noLibraries = libraries?.length === 0

  return {
    ordered,
    pinned,
    unpinned,
    anyPinned,
    anyUnpinned,
    noLibraries,
  }
}
