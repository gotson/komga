import { httpTyped } from '@/mocks/api/httpTyped'

export const matchCbl = {
  readListMatch: { name: "Jupiter's Legacy", errorCode: '' },
  requests: [
    {
      request: { series: ['Space Adventures (2018)', 'Space Adventures'], number: '1' },
      matches: [
        {
          series: {
            seriesId: '63',
            title: 'Space Adventures',
            releaseDate: '2018-07-10',
          },
          books: [{ bookId: '0F99E5W723ENS', number: '1', title: 'Volume 1' }],
        },
      ],
    },
    {
      request: { series: ["Jupiter's Legacy (2013)", "Jupiter's Legacy"], number: '2' },
      matches: [
        {
          series: {
            seriesId: '63',
            title: 'Space Adventures',
            releaseDate: '2018-07-10',
          },
          books: [{ bookId: '0F99E5W723ENS', number: '1', title: 'Volume 1' }],
        },
      ],
    },
    {
      request: { series: ["Jupiter's Legacy (2013)", "Jupiter's Legacy"], number: '3' },
      matches: [
        {
          series: {
            seriesId: '63',
            title: "Jupiter's Legacy",
            releaseDate: '2018-07-10',
          },
          books: [{ bookId: '0F99E5W763ECC', number: '3', title: 'Volume 3' }],
        },
      ],
    },
    {
      request: { series: ["Jupiter's Legacy (2013)", "Jupiter's Legacy"], number: '4' },
      matches: [
        {
          series: {
            seriesId: '63',
            title: "Jupiter's Legacy",
            releaseDate: '2018-07-10',
          },
          books: [{ bookId: '0F99E5W723ENT', number: '4', title: 'Volume 4' }],
        },
      ],
    },
    {
      request: { series: ["Jupiter's Legacy (2013)", "Jupiter's Legacy"], number: '5' },
      matches: [
        {
          series: {
            seriesId: '63',
            title: "Jupiter's Legacy",
            releaseDate: '2018-07-10',
          },
          books: [{ bookId: '0F99E5W723ENR', number: '5', title: 'Volume 5' }],
        },
      ],
    },
    {
      request: { series: ["Jupiter's Legacy 2 (2016)", "Jupiter's Legacy 2"], number: '1' },
      matches: [],
    },
    {
      request: { series: ["Jupiter's Legacy 2 (2016)", "Jupiter's Legacy 2"], number: '2' },
      matches: [],
    },
    {
      request: { series: ["Jupiter's Legacy 2 (2016)", "Jupiter's Legacy 2"], number: '3' },
      matches: [],
    },
    {
      request: { series: ["Jupiter's Legacy 2 (2016)", "Jupiter's Legacy 2"], number: '4' },
      matches: [],
    },
    {
      request: { series: ["Jupiter's Legacy 2 (2016)", "Jupiter's Legacy 2"], number: '5' },
      matches: [],
    },
    {
      request: {
        series: ["Jupiter's Legacy Requiem (2021)", "Jupiter's Legacy Requiem"],
        number: '1',
      },
      matches: [],
    },
    {
      request: {
        series: ["Jupiter's Legacy Requiem (2021)", "Jupiter's Legacy Requiem"],
        number: '2',
      },
      matches: [],
    },
    {
      request: {
        series: ["Jupiter's Legacy Requiem (2021)", "Jupiter's Legacy Requiem"],
        number: '3',
      },
      matches: [],
    },
    {
      request: {
        series: ["Jupiter's Legacy Requiem (2021)", "Jupiter's Legacy Requiem"],
        number: '4',
      },
      matches: [],
    },
    {
      request: {
        series: ["Jupiter's Legacy Requiem (2021)", "Jupiter's Legacy Requiem"],
        number: '5',
      },
      matches: [],
    },
    {
      request: {
        series: ["Jupiter's Legacy Requiem (2021)", "Jupiter's Legacy Requiem"],
        number: '6',
      },
      matches: [],
    },
  ],
  errorCode: '',
}

export const readListsHandlers = [
  httpTyped.post('/api/v1/readlists', async ({ request, response }) => {
    const body = await request.json()
    return response(200).json({
      ...body,
      createdDate: new Date(),
      lastModifiedDate: new Date(),
      id: (Math.random() + 1).toString(36).substring(7),
      filtered: false,
    })
  }),
  httpTyped.post('/api/v1/readlists/match/comicrack', ({ response }) => {
    return response(200).json(matchCbl)
  }),
]
