import { mockPage } from '@/mocks/api/pageable'
import { PageRequest } from '@/types/PageRequest'
import { http, HttpResponse } from 'msw'
import mockThumbnailUrl from '@/assets/mock-thumbnail.jpg'
import { handleGetBookById, handleGetBooks } from '@/generated/openapi/msw.gen'
import { response200OK, response404NotFound } from '@/mocks/api/utils'

export const mockBook = {
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
    title: 'Attack on Ducktropolis',
    titleLock: true,
    summary:
      'Super Duck is the greatest hero of Ducktropolis. Brash, arrogant and virtually unbeatable, he’s defeated all threats to the city and routinely foils the schemes of his greatest rival, criminal genius and corporate billionaire Dapper Duck. But now, three years later, Super Duck has fallen on hard times. Down on his luck and with his superheroing days a distant memory, he is reduced to appearing at comic conventions for measly appearance fees. So when he’s approached by a rival of Dapper to be his personal bodyguard/accompany him on his many adventures, Supe has to decide if he’s ready to don his cape once more in this series for mature readers!',
    summaryLock: false,
    number: '1',
    numberLock: false,
    numberSort: 1.0,
    numberSortLock: false,
    releaseDate: new Date('1985-10-12'),
    releaseDateLock: false,
    authors: [
      { name: 'Frank Tieri', role: 'writer' },
      { name: 'Ian Flynn', role: 'writer' },
      { name: 'Ryan Jampole', role: 'cover' },
      { name: 'Jack Morelli', role: 'penciller' },
      { name: 'Matt Herms', role: 'penciller' },
      { name: 'Matt Herms', role: 'custom' },
    ],
    authorsLock: true,
    tags: ['duck', 'adventure', 'comic'],
    tagsLock: false,
    isbn: '9781566199094',
    isbnLock: false,
    links: [{ label: 'Publisher', url: 'https://archiecomics.com/super-duck-1-of-4/' }],
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
    Object.assign({}, mockBook, {
      id: `BOOK${index + 1}`,
      name: `Book ${index + 1}`,
      number: index + 1,
      metadata: {
        title: `Book ${index + 1}`,
        number: `${index + 1}`,
        numberSort: index + 1,
        ...(index % 2 === 0 && {
          releaseDate: new Date(`19${String(index).slice(-2).padStart(2, '0')}-05-10`),
        }),
      },
    }),
  )
}

export const booksHandlers = [
  handleGetBooks(({ request }) => {
    const query = new URL(request.url).searchParams
    return response200OK(
      mockPage(
        mockBooks(50),
        new PageRequest(Number(query.get('page')), Number(query.get('size')), query.getAll('sort')),
      ),
    )
  }),
  handleGetBookById(({ params }) => {
    if (params.bookId === '404') return response404NotFound()
    return response200OK(
      Object.assign({}, mockBook, { metadata: { title: `Book ${params.bookId}` } }),
    )
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
