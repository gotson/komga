import { httpTyped } from '@/mocks/api/httpTyped'
import { mockPage } from '@/mocks/api/pageable'
import { PageRequest } from '@/types/PageRequest'
import type { components } from '@/generated/openapi/komga'
import { HttpResponse } from 'msw'
import mockThumbnailUrl from '@/assets/mock-thumbnail.jpg'
import mockThumbnailLandscapeUrl from '@/assets/mock-thumbnail-landscape.jpg'

export function mockPageHashesKnown(count: number): components['schemas']['PageHashKnownDto'][] {
  return [...Array(count).keys()].map((index) => {
    const created = new Date(`19${String(index).slice(-2).padStart(2, '0')}-05-10`)
    return {
      hash: `HASH${index}`,
      size: 1234 * (index + 1),
      action: index % 3 === 0 ? 'DELETE_AUTO' : index % 3 === 1 ? 'DELETE_MANUAL' : 'IGNORE',
      deleteCount: index % 3 === 0 ? 5 : index % 3 === 1 ? 2 : 0,
      matchCount: index * 2,
      created: created,
      lastModified: created,
    }
  })
}

export function mockPageHashMatches(count: number): components['schemas']['PageHashMatchDto'][] {
  return [...Array(count).keys()].map((index) => {
    return {
      bookId: `BOOK${index + 1}`,
      url: '/books/Super Duck/Super_Duck_001__MLJ___Fall_1944___c2c___titansfan_editor_.cbz',
      pageNumber: 25 + index,
      fileName: `Page_${25 + index}.jpg`,
      fileSize: 1234 * (index + 1),
      mediaType: 'image/jpeg',
    }
  })
}

export const pageHashesHandlers = [
  httpTyped.get('/api/v1/page-hashes', ({ query, response }) => {
    let data = mockPageHashesKnown(50)
    const actions = query.getAll('action')
    if (actions.length > 0) data = data.filter((it) => actions.includes(it.action))
    return response(200).json(
      mockPage(
        data,
        new PageRequest(Number(query.get('page')), Number(query.get('size')), query.getAll('sort')),
      ),
    )
  }),
  httpTyped.get('/api/v1/page-hashes/{pageHash}', ({ params, query, response }) => {
    const hash = params.pageHash
    const data = mockPageHashMatches(Number(hash.substring(4)) * 2)
    return response(200).json(
      mockPage(
        data,
        new PageRequest(Number(query.get('page')), Number(query.get('size')), query.getAll('sort')),
      ),
    )
  }),
  httpTyped.put('/api/v1/page-hashes', ({ response }) => {
    return response(202).empty()
  }),
  httpTyped.post('/api/v1/page-hashes/{pageHash}/delete-all', ({ response }) => {
    return response(202).empty()
  }),
  httpTyped.get('/api/v1/page-hashes/{pageHash}/thumbnail', async ({ params, response }) => {
    const hash = params.pageHash

    // use landscape image for some images
    const landscape = Number(hash.slice(-1)) % 2 === 0

    // Get an ArrayBuffer from reading the file from disk or fetching it.
    const buffer = await fetch(landscape ? mockThumbnailLandscapeUrl : mockThumbnailUrl).then(
      (response) => response.arrayBuffer(),
    )

    return response.untyped(
      HttpResponse.arrayBuffer(buffer, {
        status: 200,
        headers: {
          'content-type': 'image/jpg',
        },
      }),
    )
  }),
]
