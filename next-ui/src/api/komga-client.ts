import type { Middleware } from 'openapi-fetch'
import createClient from 'openapi-fetch'
import type { paths } from '@/generated/openapi/komga'
import { ApiBaseUrl } from '@/api/base'
import * as v from 'valibot'

// Middleware that throws on error, so it works with Pinia Colada
const coladaMiddleware: Middleware = {
  async onResponse({ response }: { response: Response }) {
    if (!response.ok) {
      let body: SpringError = {}
      try {
        body = await response.json()
      } catch (ignoreErr) {}
      throw new Error(`${response.url}: ${response.status} ${response.statusText}`, {
        cause: {
          body: body,
          status: response.status,
          message: body?.message,
        },
      })
    }
    // return response untouched
    return undefined
  },
  onError() {
    throw new Error('error', { cause: {} })
  },
}

const client = createClient<paths>({
  baseUrl: ApiBaseUrl.noSlash,
  // required to pass the session cookie on all requests
  credentials: 'include',
  // required to avoid browser basic-auth popups
  headers: { 'X-Requested-With': 'XMLHttpRequest' },
})
client.use(coladaMiddleware)

type SpringError = {
  message?: string
}

export type ErrorCause = {
  body?: unknown
  status?: number
  message?: string
}

const ErrorSchema = v.looseObject({
  message: v.optional(v.string()),
  cause: v.optional(
    v.object({
      body: v.looseObject({}),
      status: v.number(),
      message: v.optional(v.string()),
    }),
  ),
})

export function isError(item: unknown): item is v.InferOutput<typeof ErrorSchema> {
  return v.is(ErrorSchema, item)
}

export const komgaClient = client
