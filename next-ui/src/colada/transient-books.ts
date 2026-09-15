import { defineQueryOptions } from '@pinia/colada'
import { komgaAnalyzeTransientBook, komgaScanTransientBooks } from '@/generated/openapi'
import { STALE_TIME } from '@/types/time'

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
  staleTime: STALE_TIME.LONG,
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
    staleTime: STALE_TIME.LONG,
  }),
)
