import { PageRequest } from '@/types/PageRequest'
import { mockPage } from '@/mocks/api/pageable'
import { http, HttpResponse } from 'msw'
import mockThumbnailUrl from '@/assets/mock-thumbnail.jpg'
import { handleGetCollections, handleGetCollectionThumbnails } from '@/generated/openapi/msw.gen'

import { response200OK } from '@/mocks/api/utils'

export const mockCollection1 = {
  id: '026801S4HWRZA',
  name: 'Golden Age',
  ordered: true,
  seriesIds: ['57'],
  createdDate: new Date('2020-08-06T06:13:25Z'),
  lastModifiedDate: new Date('2020-08-06T06:17:12Z'),
  filtered: false,
}

const mockCollection2 = {
  id: '026801S4HWRZB',
  name: 'Iron Age',
  ordered: false,
  seriesIds: ['57'],
  createdDate: new Date('2020-08-06T06:13:25Z'),
  lastModifiedDate: new Date('2020-08-06T06:17:12Z'),
  filtered: false,
}

const collections = [mockCollection1, mockCollection2]

export function mockCollections(count: number) {
  return [...Array(count).keys()].map((index) =>
    Object.assign({}, mockCollection1, {
      id: `COL${index + 1}`,
      name: `Collection ${index + 1}`,
    }),
  )
}

export const collectionsHandlers = [
  handleGetCollections(({ request }) => {
    const query = new URL(request.url).searchParams
    const search = query.get('search')

    const selected = collections.filter((it) => {
      let include = true
      if (search) include = include && !!it.name.match(new RegExp(search, 'i'))
      return include
    })

    return response200OK(
      mockPage(selected, new PageRequest(Number(query.get('page')), Number(query.get('size')))),
    )
  }),
  handleGetCollectionThumbnails(({ params }) =>
    response200OK([
      {
        collectionId: params.id,
        fileSize: 1524,
        height: 1300,
        width: 1250,
        mediaType: 'image/avif',
        id: '1',
        selected: true,
        type: 'USER_UPLOADED',
      },
      {
        collectionId: params.id,
        fileSize: 1524,
        height: 300,
        width: 250,
        mediaType: 'image/jpeg',
        id: '2',
        selected: false,
        type: 'GENERATED',
      },
    ]),
  ),
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
