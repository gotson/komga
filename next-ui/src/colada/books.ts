import { defineQueryOptions } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'

export const QUERY_KEYS_BOOKS = {
  root: ['books'] as const,
  byId: (bookId: string) => [...QUERY_KEYS_BOOKS.root, bookId] as const,
}

export const bookDetailQuery = defineQueryOptions(({ bookId }: { bookId: string }) => ({
  key: QUERY_KEYS_BOOKS.byId(bookId),
  query: () =>
    komgaClient
      .GET('/api/v1/books/{bookId}', {
        params: {
          path: {
            bookId: bookId,
          },
        },
      })
      // unwrap the openapi-fetch structure on success
      .then((res) => res.data),
}))
