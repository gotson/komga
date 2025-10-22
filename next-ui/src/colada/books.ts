import { defineQueryOptions } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'
import type { components } from '@/generated/openapi/komga'
import type { PageRequest } from '@/types/PageRequest'

export const QUERY_KEYS_BOOKS = {
  root: ['books'] as const,
  bySearch: (request: object) => [...QUERY_KEYS_BOOKS.root, JSON.stringify(request)] as const,
  byId: (bookId: string) => [...QUERY_KEYS_BOOKS.root, bookId] as const,
}

export const bookListQuery = defineQueryOptions(
  ({
    search,
    pause = false,
    pageRequest,
  }: {
    search: components['schemas']['BookSearch']
    pause?: boolean
    pageRequest?: PageRequest
  }) => ({
    key: QUERY_KEYS_BOOKS.bySearch({ search: search, pageRequest: pageRequest }),
    query: () =>
      komgaClient
        .POST('/api/v1/books/list', {
          body: search,
          params: {
            query: {
              ...pageRequest,
            },
          },
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
