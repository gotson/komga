import { actuatorHandlers } from '@/mocks/api/handlers/actuator'
import { announcementHandlers } from '@/mocks/api/handlers/announcements'
import { releasesHandlers } from '@/mocks/api/handlers/releases'
import { HttpResponse } from 'msw'
import { librariesHandlers } from '@/mocks/api/handlers/libraries'
import { referentialHandlers } from '@/mocks/api/handlers/referential'
import { usersHandlers } from '@/mocks/api/handlers/users'
import { settingsHandlers } from '@/mocks/api/handlers/settings'
import { claimHandlers } from '@/mocks/api/handlers/claim'

export const handlers = [
  ...actuatorHandlers,
  ...announcementHandlers,
  ...claimHandlers,
  ...librariesHandlers,
  ...referentialHandlers,
  ...releasesHandlers,
  ...settingsHandlers,
  ...usersHandlers,
]

export const response401Unauthorized = () =>
  HttpResponse.json({ error: 'Unauthorized' }, { status: 401 })

export const response502BadGateway = () =>
  HttpResponse.json({ error: 'Bad gateway' }, { status: 502 })
