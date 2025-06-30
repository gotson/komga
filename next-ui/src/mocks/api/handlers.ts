import { actuatorHandlers } from '@/mocks/api/handlers/actuator'
import { announcementHandlers } from '@/mocks/api/handlers/announcements'
import { releasesHandlers } from '@/mocks/api/handlers/releases'
import { HttpResponse } from 'msw'

export const handlers = [...actuatorHandlers, ...announcementHandlers, ...releasesHandlers]

export const response401Unauthorized = () =>
  HttpResponse.json({ error: 'Unauthorized' }, { status: 401 })
