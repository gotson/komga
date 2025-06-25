import { createOpenApiHttp } from 'openapi-msw'
import type { paths } from '@/generated/openapi/komga'

export const httpTyped = createOpenApiHttp<paths>({ baseUrl: import.meta.env.VITE_KOMGA_API_URL })
