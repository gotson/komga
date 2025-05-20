import type {Middleware} from 'openapi-fetch'
import createClient from 'openapi-fetch'
import type {paths} from '@/generated/openapi/komga'

// Middleware that throws on error, so it works with Pinia Colada
const coladaMiddleware: Middleware = {
  async onResponse({response}: {response: Response}) {
    if (!response.ok)
      throw new Error(`${response.url}: ${response.status} ${response.statusText}`)
    // return response untouched
    return undefined
  },
}

const client = createClient<paths>({
  baseUrl: 'http://localhost:8080',
  // required to pass the session cookie on all requests
  credentials: 'include',
  // required to avoid browser basic-auth popups
  headers: {'X-Requested-With': 'XMLHttpRequest'},
})
client.use(coladaMiddleware)

export const komgaClient = client
