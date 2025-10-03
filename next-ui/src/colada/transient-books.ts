import { defineQueryOptions } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'

export const QUERY_KEYS_TRANSIENT_BOOKS = {
  root: ['transient-books'] as const,
  byPath: (path: string) => [...QUERY_KEYS_TRANSIENT_BOOKS.root, path] as const,
  byId: (transientBookId: string) => [...QUERY_KEYS_TRANSIENT_BOOKS.root, transientBookId] as const,
}

export const transientBooksScan = defineQueryOptions(({ path }: { path: string }) => ({
  key: QUERY_KEYS_TRANSIENT_BOOKS.byPath(path),
  enabled: path.length > 0,
  query: () =>
    komgaClient
      .POST('/api/v1/transient-books', {
        body: {
          path: path,
        },
      })
      // unwrap the openapi-fetch structure on success
      .then((res) => res.data),
}))

export const transientBookAnalyze = defineQueryOptions(
  ({ transientBookId }: { transientBookId: string }) => ({
    key: QUERY_KEYS_TRANSIENT_BOOKS.byId(transientBookId),
    query: () =>
      komgaClient
        .POST('/api/v1/transient-books/{id}/analyze', {
          params: {
            path: {
              id: transientBookId,
            },
          },
        })
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
  }),
)
