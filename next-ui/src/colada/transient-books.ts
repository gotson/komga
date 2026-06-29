import { defineQueryOptions } from '@pinia/colada'
import { komgaAnalyzeTransientBook, komgaScanTransientBooks } from '@/generated/openapi'

export const QUERY_KEYS_TRANSIENT_BOOKS = {
  root: ['transient-books'] as const,
  byPath: (path: string) => [...QUERY_KEYS_TRANSIENT_BOOKS.root, path] as const,
  byId: (transientBookId: string) => [...QUERY_KEYS_TRANSIENT_BOOKS.root, transientBookId] as const,
}

export const transientBooksScan = defineQueryOptions(({ path }: { path: string }) => ({
  key: QUERY_KEYS_TRANSIENT_BOOKS.byPath(path),
  enabled: path.length > 0,
  query: () =>
    komgaScanTransientBooks({
      body: {
        path: path,
      },
    }),
  // 1 hour
  staleTime: 60 * 60 * 1000,
}))

export const transientBookAnalyze = defineQueryOptions(
  ({ transientBookId }: { transientBookId: string }) => ({
    key: QUERY_KEYS_TRANSIENT_BOOKS.byId(transientBookId),
    query: () =>
      komgaAnalyzeTransientBook({
        path: {
          id: transientBookId,
        },
      }),
    // 1 hour
    staleTime: 60 * 60 * 1000,
  }),
)
