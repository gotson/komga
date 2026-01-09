import { httpTyped } from '@/mocks/api/httpTyped'
import { http, HttpResponse } from 'msw'
import mockThumbnailUrl from '@/assets/mock-thumbnail.jpg'
import { PageRequest } from '@/types/PageRequest'
import { mockPage } from '@/mocks/api/pageable'

export const mockSeries1 = {
  id: '57',
  libraryId: '1',
  name: 'Super Duck',
  url: '/books/Super Duck',
  created: new Date('2020-07-05T12:11:50Z'),
  lastModified: new Date('2021-07-27T13:33:54Z'),
  fileLastModified: new Date('2020-03-05T15:24:59Z'),
  booksCount: 5,
  booksReadCount: 1,
  booksUnreadCount: 3,
  booksInProgressCount: 1,
  metadata: {
    status: 'ENDED',
    statusLock: true,
    title: 'Super Duck',
    titleLock: false,
    titleSort: 'Super Duck',
    titleSortLock: false,
    summary:
      'Super Duck is the greatest hero of New Duck City. Brash, arrogant and virtually unbeatable, he’s defeated all threats to the city and routinely foils the schemes of his greatest rival, criminal genius and corporate billionaire Dapper Duck.\n\nHowever, when Dapper takes to the streets with a giant mechanical monster, will Super Duck prove once more to be the heroic champion everyone knows and loves or is his goose finally cooked?',
    summaryLock: true,
    readingDirection: 'LEFT_TO_RIGHT',
    readingDirectionLock: true,
    publisher: 'Archie',
    publisherLock: true,
    ageRatingLock: false,
    language: 'en',
    languageLock: true,
    genres: ['humor'],
    genresLock: true,
    tags: [],
    tagsLock: false,
    totalBookCount: 94,
    totalBookCountLock: true,
    sharingLabels: [],
    sharingLabelsLock: false,
    links: [],
    linksLock: false,
    alternateTitles: [],
    alternateTitlesLock: false,
    created: new Date('2020-07-05T12:11:50Z'),
    lastModified: new Date('2023-07-22T11:14:45Z'),
  },
  booksMetadata: {
    authors: [],
    tags: [],
    summary: '',
    summaryNumber: '',
    created: new Date('2021-01-11T09:59:23Z'),
    lastModified: new Date('2023-07-22T11:14:45Z'),
  },
  deleted: false,
  oneshot: false,
}

const series2 = {
  id: '63',
  libraryId: '2',
  name: 'Space Adventures',
  url: '/books/Space Adventures',
  created: new Date('2020-07-05T12:11:50Z'),
  lastModified: new Date('2020-07-05T12:11:50Z'),
  fileLastModified: new Date('2020-03-05T11:57:31Z'),
  booksCount: 4,
  booksReadCount: 0,
  booksUnreadCount: 3,
  booksInProgressCount: 1,
  metadata: {
    status: 'ENDED',
    statusLock: true,
    title: 'Space Adventures',
    titleLock: false,
    titleSort: 'Space Adventures',
    titleSortLock: false,
    summary:
      'Supersophisticated androids that can pass for human? Robots that turn on their creators to take control of their world? Strange alien armies secretly infiltrating the earth? Men rocketing through the galaxy as easily as taking an average Sunday drive in the country? Come on, that stuff is just a bunch of science fiction, right?\n\nYou bet it is! Published every two months, Charlton Comics presented a new collection of short stories about mankind’s long-dreamed-of exploration of the rest of the solar system…and beyond!\n\nThis series is notable for its many stories by Steve Ditko (creator of The Amazing Spider-Man), and for the first appearance of Captain Atom.',
    summaryLock: true,
    readingDirection: 'LEFT_TO_RIGHT',
    readingDirectionLock: true,
    publisher: 'Charlton',
    publisherLock: true,
    ageRatingLock: false,
    language: 'en',
    languageLock: true,
    genres: ['science fiction'],
    genresLock: true,
    tags: [],
    tagsLock: false,
    totalBookCount: 70,
    totalBookCountLock: true,
    sharingLabels: [],
    sharingLabelsLock: false,
    links: [],
    linksLock: false,
    alternateTitles: [],
    alternateTitlesLock: false,
    created: new Date('2020-07-05T12:11:50Z'),
    lastModified: new Date('2023-07-22T11:14:45Z'),
  },
  booksMetadata: {
    authors: [],
    tags: [],
    releaseDate: '2018-07-10',
    summary: '',
    summaryNumber: '',
    created: new Date('2021-01-11T09:59:23Z'),
    lastModified: new Date('2025-04-08T02:55:19Z'),
  },
  deleted: false,
  oneshot: false,
}

const series = [mockSeries1, series2]

export const seriesHandlers = [
  httpTyped.post('/api/v1/series/list', async ({ query, request, response }) => {
    const body = await request.json()

    const selectedSeries = body.fullTextSearch
      ? series.filter((it) => !!it.metadata.title.match(new RegExp(body.fullTextSearch!, 'i')))
      : series

    return response(200).json(
      mockPage(
        selectedSeries,
        new PageRequest(Number(query.get('page')), Number(query.get('size')), query.getAll('sort')),
      ),
    )
  }),
  httpTyped.get('/api/v1/series/{seriesId}', ({ params, response }) => {
    if (params.seriesId === '404') return response(404).empty()
    return response(200).json(
      Object.assign({}, mockSeries1, { metadata: { title: `Series ${params.seriesId}` } }),
    )
  }),
  http.get('*/api/v1/series/*/thumbnail', async () => {
    // Get an ArrayBuffer from reading the file from disk or fetching it.
    const buffer = await fetch(mockThumbnailUrl).then((response) => response.arrayBuffer())

    return HttpResponse.arrayBuffer(buffer, {
      headers: {
        'content-type': 'image/jpg',
      },
    })
  }),
]
