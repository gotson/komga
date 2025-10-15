import { createOpenApiHttp } from 'openapi-msw'
import type { paths } from '@/generated/openapi/komga'
import { API_BASE_URL } from '@/api/base'

export const httpTyped = createOpenApiHttp<paths>({ baseUrl: API_BASE_URL })
