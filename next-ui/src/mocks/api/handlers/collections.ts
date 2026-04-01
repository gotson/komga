import { httpTyped } from '@/mocks/api/httpTyped'
import { PageRequest } from '@/types/PageRequest'
import { mockPage } from '@/mocks/api/pageable'
import { http, HttpResponse } from 'msw'
import mockThumbnailUrl from '@/assets/mock-thumbnail.jpg'

export const mockCollection = {
  id: '026801S4HWRZA',
  name: 'Golden Age',
  ordered: true,
  seriesIds: ['57'],
  createdDate: new Date('2020-08-06T06:13:25Z'),
  lastModifiedDate: new Date('2020-08-06T06:17:12Z'),
  filtered: false,
}

const collections = [mockCollection]

export const collectionsHandlers = [
  httpTyped.get('/api/v1/collections', ({ query, response }) => {
    const search = query.get('search')

    const selected = collections.filter((it) => {
      let include = true
      if (search) include = include && !!it.name.match(new RegExp(search, 'i'))
      return include
    })

    return response(200).json(
      mockPage(selected, new PageRequest(Number(query.get('page')), Number(query.get('size')))),
    )
  }),
  http.get('*/api/v1/collections/*/thumbnail', async () => {
    // Get an ArrayBuffer from reading the file from disk or fetching it.
    const buffer = await fetch(mockThumbnailUrl).then((response) => response.arrayBuffer())

    return HttpResponse.arrayBuffer(buffer, {
      headers: {
        'content-type': 'image/jpg',
      },
    })
  }),
]
