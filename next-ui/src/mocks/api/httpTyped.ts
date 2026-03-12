import { createOpenApiHttp } from 'openapi-msw'
import type { paths } from '@/generated/openapi/komga'
import { ApiBaseUrl } from '@/api/base'

export const httpTyped = createOpenApiHttp<paths>({ baseUrl: ApiBaseUrl.noSlash })
