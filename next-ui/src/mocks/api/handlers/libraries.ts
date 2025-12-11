import { httpTyped } from '@/mocks/api/httpTyped'
import type { components } from '@/generated/openapi/komga'
import { response400BadRequest, response404NotFound } from '@/mocks/api/handlers'

export const libraries = [
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
  } as components['schemas']['LibraryDto'],
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
  } as components['schemas']['LibraryDto'],
]

export const librariesHandlers = [
  httpTyped.get('/api/v1/libraries', ({ response }) => response(200).json(libraries)),
  httpTyped.post('/api/v1/libraries', async ({ request, response }) => {
    const body = await request.json()

    if (libraries.some((it) => it.id === body.name)) {
      return response.untyped(response400BadRequest())
    }

    const lib = Object.assign({}, body, { unavailable: false, id: body.name })
    libraries.push(lib)

    return response(200).json(lib)
  }),
  httpTyped.patch('/api/v1/libraries/{libraryId}', async ({ request, params, response }) => {
    const body = await request.json()
    const libraryId = params['libraryId']

    const existing = libraries.find((it) => it.id === libraryId)

    if (!existing) {
      return response.untyped(response404NotFound())
    }

    libraries[libraries.indexOf(existing)] = Object.assign({}, existing, body)

    return response(204).empty()
  }),
]
