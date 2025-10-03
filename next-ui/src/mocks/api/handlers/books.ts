import { httpTyped } from '@/mocks/api/httpTyped'
import { mockPage } from '@/mocks/api/pageable'
import { PageRequest } from '@/types/PageRequest'
import { http, HttpResponse } from 'msw'
import mockThumbnailUrl from '@/assets/mock-thumbnail.jpg'

const book = {
  id: '05RKH8CC8B4RW',
  seriesId: '57',
  seriesTitle: 'Super Duck',
  libraryId: '56',
  name: 'Super_Duck_001__MLJ___Fall_1944___c2c___titansfan_editor_',
  url: '/books/Super Duck/Super_Duck_001__MLJ___Fall_1944___c2c___titansfan_editor_.cbz',
  number: 1,
  created: new Date('2021-07-27T13:33:52Z'),
  lastModified: new Date('2023-07-22T11:14:47Z'),
  fileLastModified: new Date('2020-03-05T03:20:12Z'),
  sizeBytes: 82920938,
  size: '79.1 MiB',
  media: {
    status: 'READY',
    mediaType: 'application/zip',
    pagesCount: 53,
    comment: '',
    epubDivinaCompatible: false,
    epubIsKepub: false,
    mediaProfile: 'DIVINA',
  },
  metadata: {
    title: 'Super Duck 001',
    titleLock: true,
    summary: '',
    summaryLock: false,
    number: '1',
    numberLock: false,
    numberSort: 1.0,
    numberSortLock: false,
    releaseDateLock: false,
    authors: [],
    authorsLock: true,
    tags: [],
    tagsLock: false,
    isbn: '',
    isbnLock: false,
    links: [],
    linksLock: false,
    created: new Date('2021-07-27T13:33:52Z'),
    lastModified: new Date('2023-03-15T09:50:54Z'),
  },
  readProgress: {
    page: 53,
    completed: true,
    readDate: new Date('2025-02-19T11:29:25Z'),
    created: new Date('2025-02-19T11:29:25Z'),
    lastModified: new Date('2025-02-19T11:29:25Z'),
    deviceId: '',
    deviceName: '',
  },
  deleted: false,
  fileHash: '7dc12ae431a8847b7f49918745254b0b',
  oneshot: false,
}

export function mockBooks(count: number) {
  return [...Array(count).keys()].map((index) =>
    Object.assign({}, book, {
      id: `BOOK${index + 1}`,
      name: `Book ${index + 1}`,
      number: index + 1,
      metadata: {
        title: `Book ${index + 1}`,
        number: `${index + 1}`,
        numberSort: index + 1,
        ...(index % 2 === 0 && {
          releaseDate: `19${String(index).slice(-2).padStart(2, '0')}-05-10`,
        }),
      },
    }),
  )
}

export const booksHandlers = [
  httpTyped.post('/api/v1/books/list', ({ query, response }) => {
    return response(200).json(
      mockPage(
        mockBooks(50),
        new PageRequest(Number(query.get('page')), Number(query.get('size')), query.getAll('sort')),
      ),
    )
  }),
  httpTyped.get('/api/v1/books/{bookId}', ({ params, response }) => {
    if (params.bookId === '404') return response(404).empty()
    return response(200).json(
      Object.assign({}, book, { metadata: { title: `Book ${params.bookId}` } }),
    )
  }),
  httpTyped.post('/api/v1/books/import', ({ response }) => {
    return response(202).empty()
  }),
  http.get('*/api/v1/books/*/thumbnail', async () => {
    // Get an ArrayBuffer from reading the file from disk or fetching it.
    const buffer = await fetch(mockThumbnailUrl).then((response) => response.arrayBuffer())

    return HttpResponse.arrayBuffer(buffer, {
      headers: {
        'content-type': 'image/jpg',
      },
    })
  }),
]
