import { ApiBaseUrl } from '@/api/base'
import * as v from 'valibot'
import { client } from '@/generated/openapi/client.gen'

type SpringError = {
  message?: string
}

const ApiErrorCause = v.object({
  status: v.number(),
  message: v.optional(v.string()),
  body: v.unknown(),
})

export type ApiErrorCause = v.InferOutput<typeof ApiErrorCause>

export function isApiErrorWithCause(error: unknown): error is Error & { cause: ApiErrorCause } {
  return error instanceof Error && v.is(ApiErrorCause, error.cause)
}

const responseInterceptor = async (response: Response) => {
  if (!response.ok) {
    let body: SpringError = {}
    try {
      // Clone the response so we don't consume the readable stream
      // in case another interceptor needs it
      body = await response
        .clone()
        .json()
        .catch(() => null)
    } catch (ignoreErr) {}
    throw new Error(`${response.url}: ${response.status} ${response.statusText}`, {
      cause: {
        body: body,
        status: response.status,
        message: body?.message,
      },
    })
  }
  return response
}

const errorInterceptor = (e: unknown) => {
  if (isApiErrorWithCause(e)) throw e
  throw new Error('error', { cause: {} })
}

export function setupOpenapiClient() {
  client.setConfig({
    baseUrl: ApiBaseUrl.noSlash,
    // required to pass the session cookie on all requests
    credentials: 'include',
    // required to avoid browser basic-auth popups
    headers: { 'X-Requested-With': 'XMLHttpRequest' },
    throwOnError: true,
  })
  client.interceptors.response.use(responseInterceptor)
  client.interceptors.error.use(errorInterceptor)
}
