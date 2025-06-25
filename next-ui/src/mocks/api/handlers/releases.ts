import { httpTyped } from '@/mocks/api/httpTyped'

export const releasesResponseOk = [
  {
    version: '1.21.2',
    releaseDate: new Date('2025-03-12T04:19:30Z'),
    url: 'https://github.com/gotson/komga/releases/tag/1.21.2',
    latest: true,
    preRelease: false,
    description: 'Truncated',
  },
  {
    version: '1.21.1',
    releaseDate: new Date('2025-03-06T07:31:00Z'),
    url: 'https://github.com/gotson/komga/releases/tag/1.21.1',
    latest: false,
    preRelease: false,
    description: 'Truncated',
  },
]

export const releasesResponseOkNotLatest = [
  {
    version: '1.21.3',
    releaseDate: new Date('2025-03-06T07:31:00Z'),
    url: 'https://github.com/gotson/komga/releases/tag/1.21.1',
    latest: true,
    preRelease: false,
    description: 'Truncated',
  },
]

export const releasesHandlers = [
  httpTyped.get('/api/v1/releases', ({ response }) => response(200).json(releasesResponseOk)),
]
