import { announcementHandlers } from '@/mocks/api/handlers/announcements'
import { releasesHandlers } from '@/mocks/api/handlers/releases'
import { fromOpenApi } from '@mswjs/source/open-api'
import type { OpenAPIV3 } from 'openapi-types'
import spec from '../../../../komga/docs/openapi.json'
import { HttpResponse } from 'msw'

const doc = {
  basePath: import.meta.env.VITE_KOMGA_API_URL,
  ...spec,
} as unknown as OpenAPIV3.Document

// manually defined handlers need to be before fromOpenApi
export const handlers = [...announcementHandlers, ...releasesHandlers, ...(await fromOpenApi(doc))]

export const response401Unauthorized = () =>
  HttpResponse.json({ error: 'Unauthorized' }, { status: 401 })
