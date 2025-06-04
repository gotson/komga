import type { Middleware } from 'openapi-fetch'
import createClient from 'openapi-fetch'
import type { paths } from 'openapi/komga'

// Middleware that throws on error, so it works with Pinia Colada
const coladaMiddleware: Middleware = {
  async onResponse({ response }: { response: Response }) {
    if (!response.ok) {
      let body: unknown
      try {
        body = await response.json()
      } catch (ignoreErr) {}
      throw new Error(`${response.url}: ${response.status} ${response.statusText}`, {
        cause: {
          body: body,
          status: response.status,
          message: 'Invalid authentication',
        },
      })
    }
    // return response untouched
    return undefined
  },
  onError() {
    throw new Error('error', {
      cause: {
        message: 'Server is unreachable',
      },
    })
  },
}

const client = createClient<paths>({
  baseUrl: 'http://localhost:8080',
  // required to pass the session cookie on all requests
  credentials: 'include',
  // required to avoid browser basic-auth popups
  headers: { 'X-Requested-With': 'XMLHttpRequest' },
})
client.use(coladaMiddleware)

export interface ErrorCause {
  body?: unknown
  status?: number
  message?: string
}

export const komgaClient = client
