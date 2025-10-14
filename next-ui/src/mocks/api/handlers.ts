import { actuatorHandlers } from '@/mocks/api/handlers/actuator'
import { announcementHandlers } from '@/mocks/api/handlers/announcements'
import { releasesHandlers } from '@/mocks/api/handlers/releases'
import { HttpResponse, type JsonBodyType } from 'msw'
import { librariesHandlers } from '@/mocks/api/handlers/libraries'
import { referentialHandlers } from '@/mocks/api/handlers/referential'
import { usersHandlers } from '@/mocks/api/handlers/users'
import { settingsHandlers } from '@/mocks/api/handlers/settings'
import { claimHandlers } from '@/mocks/api/handlers/claim'
import { historyHandlers } from '@/mocks/api/handlers/history'
import { seriesHandlers } from '@/mocks/api/handlers/series'
import { booksHandlers } from '@/mocks/api/handlers/books'
import { filesystemHandlers } from '@/mocks/api/handlers/filesystem'
import { transientBooksHandlers } from '@/mocks/api/handlers/transient-books'
import { readListsHandlers } from '@/mocks/api/handlers/readlists'

export const handlers = [
  ...actuatorHandlers,
  ...announcementHandlers,
  ...booksHandlers,
  ...claimHandlers,
  ...filesystemHandlers,
  ...historyHandlers,
  ...librariesHandlers,
  ...readListsHandlers,
  ...referentialHandlers,
  ...releasesHandlers,
  ...seriesHandlers,
  ...settingsHandlers,
  ...transientBooksHandlers,
  ...usersHandlers,
]

export const response400BadRequest = () =>
  HttpResponse.json({ error: 'Bad Request' }, { status: 400 })

export const response400 = (body: JsonBodyType) => HttpResponse.json(body, { status: 400 })

export const response404NotFound = () => HttpResponse.json({ error: 'NotFound' }, { status: 404 })

export const response401Unauthorized = () =>
  HttpResponse.json({ error: 'Unauthorized' }, { status: 401 })

export const response502BadGateway = () =>
  HttpResponse.json({ error: 'Bad gateway' }, { status: 502 })
