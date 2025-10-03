import { defineQueryOptions } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'
import type { components } from '@/generated/openapi/komga'

export const QUERY_KEYS_BOOKS = {
  root: ['books'] as const,
  bySearch: (search: components['schemas']['BookSearch']) =>
    [...QUERY_KEYS_BOOKS.root, JSON.stringify(search)] as const,
  byId: (bookId: string) => [...QUERY_KEYS_BOOKS.root, bookId] as const,
}

export const bookListQuery = defineQueryOptions(
  ({
    search,
    pause = false,
  }: {
    search: components['schemas']['BookSearch']
    pause?: boolean
  }) => ({
    key: QUERY_KEYS_BOOKS.bySearch(search),
    query: () =>
      komgaClient
        .POST('/api/v1/books/list', {
          body: search,
        })
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
    enabled: !pause,
    placeholderData: (previousData: any) => previousData, // eslint-disable-line @typescript-eslint/no-explicit-any
  }),
)

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
