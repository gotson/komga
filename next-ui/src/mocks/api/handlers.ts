import { actuatorHandlers } from '@/mocks/api/handlers/actuator'
import { announcementHandlers } from '@/mocks/api/handlers/announcements'
import { releasesHandlers } from '@/mocks/api/handlers/releases'
import { HttpResponse } from 'msw'
import { librariesHandlers } from '@/mocks/api/handlers/libraries'
import { referentialHandlers } from '@/mocks/api/handlers/referential'
import { usersHandlers } from '@/mocks/api/handlers/users'

export const handlers = [
  ...librariesHandlers,
  ...referentialHandlers,
  ...actuatorHandlers,
  ...announcementHandlers,
  ...releasesHandlers,
  ...usersHandlers,
]

export const response401Unauthorized = () =>
  HttpResponse.json({ error: 'Unauthorized' }, { status: 401 })
