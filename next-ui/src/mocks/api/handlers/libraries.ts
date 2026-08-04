import type { LibraryDto } from '@/generated/openapi'
import {
  handleAddLibrary,
  handleDeleteLibraryById,
  handleGetLibraries,
  handleUpdateLibraryById,
} from '@/generated/openapi/msw.gen'
import {
  response200OK,
  response204Empty,
  response400BadRequest,
  response404NotFound,
} from '@/mocks/api/utils'

export const mockLibraries = [
  {
    id: '1',
    name: 'eBooks',
    root: '/ebooks',
    importComicInfoBook: true,
    importComicInfoSeries: true,
    importComicInfoCollection: true,
    importComicInfoReadList: true,
    importComicInfoSeriesAppendVolume: true,
    importEpubBook: true,
    importEpubSeries: true,
    importMylarSeries: true,
    importLocalArtwork: true,
    importBarcodeIsbn: false,
    scanForceModifiedTime: false,
    scanInterval: 'DISABLED',
    scanOnStartup: false,
    scanCbx: true,
    scanPdf: true,
    scanEpub: true,
    scanDirectoryExclusions: ['#recycle', '@Recycle', '@eaDir'],
    repairExtensions: false,
    convertToCbz: false,
    emptyTrashAfterScan: true,
    seriesCover: 'FIRST',
    hashFiles: true,
    hashPages: false,
    hashKoreader: false,
    analyzeDimensions: false,
    oneshotsDirectory: '_oneshots',
    unavailable: false,
  } as LibraryDto,
  {
    id: '2',
    name: 'Comics',
    root: '/books',
    importComicInfoBook: true,
    importComicInfoSeries: true,
    importComicInfoCollection: true,
    importComicInfoReadList: true,
    importComicInfoSeriesAppendVolume: true,
    importEpubBook: true,
    importEpubSeries: true,
    importMylarSeries: true,
    importLocalArtwork: true,
    importBarcodeIsbn: true,
    scanForceModifiedTime: false,
    scanInterval: 'DISABLED',
    scanOnStartup: false,
    scanCbx: true,
    scanPdf: true,
    scanEpub: true,
    scanDirectoryExclusions: ['#recycle', '@Recycle', '@eaDir'],
    repairExtensions: false,
    convertToCbz: false,
    emptyTrashAfterScan: true,
    seriesCover: 'FIRST_UNREAD_OR_FIRST',
    hashFiles: true,
    hashPages: false,
    hashKoreader: false,
    analyzeDimensions: true,
    unavailable: false,
  } as LibraryDto,
]

export const librariesHandlers = [
  handleGetLibraries(() => response200OK(mockLibraries)),
  handleAddLibrary(async ({ request }) => {
    const body = await request.json()

    if (mockLibraries.some((it) => it.id === body.name)) {
      return response400BadRequest()
    }

    const lib = Object.assign({}, body, { unavailable: false, id: body.name })
    mockLibraries.push(lib)

    return response200OK(lib)
  }),
  handleUpdateLibraryById(async ({ request, params }) => {
    const body = await request.json()
    const libraryId = params['libraryId']

    const existing = mockLibraries.find((it) => it.id === libraryId)

    if (!existing) {
      return response404NotFound()
    }

    mockLibraries[mockLibraries.indexOf(existing)] = Object.assign({}, existing, body)

    return response204Empty()
  }),
  handleDeleteLibraryById(({ params }) => {
    const libraryId = params['libraryId']

    const existing = mockLibraries.find((it) => it.id === libraryId)

    if (!existing) {
      return response404NotFound()
    }

    mockLibraries.splice(mockLibraries.indexOf(existing), 1)

    return response204Empty()
  }),
]
