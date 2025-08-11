import { httpTyped } from '@/mocks/api/httpTyped'

const series = {
  id: '57',
  libraryId: '56',
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

export const seriesHandlers = [
  httpTyped.get('/api/v1/series/{seriesId}', ({ params, response }) => {
    if (params.seriesId === '404') return response(404).empty()
    return response(200).json(
      Object.assign({}, series, { metadata: { title: `Series ${params.seriesId}` } }),
    )
  }),
]
