import { http, HttpResponse } from 'msw'
import { baseUrl } from '@/mocks/api/handlers/base'

export const releasesHandlers = [
  http.get(baseUrl + 'api/v1/releases', () => {
    return HttpResponse.json([
      {
        version: '9.9.9',
        releaseDate: '2025-05-16T04:31:05Z',
        url: 'https://github.com/gotson/komga/releases/tag/1.21.3',
        latest: true,
        preRelease: false,
        description: 'Truncated',
      },
      {
        version: '1.21.2',
        releaseDate: '2025-03-12T04:19:30Z',
        url: 'https://github.com/gotson/komga/releases/tag/1.21.2',
        latest: false,
        preRelease: false,
        description: 'Truncated',
      },
      {
        version: '1.21.1',
        releaseDate: '2025-03-06T07:31:00Z',
        url: 'https://github.com/gotson/komga/releases/tag/1.21.1',
        latest: false,
        preRelease: false,
        description: 'Truncated',
      },
    ])
  }),
]
