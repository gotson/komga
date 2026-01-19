import { defineMutation, defineQueryOptions, useMutation } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'
import type { components } from '@/generated/openapi/komga'
import type { PageRequest } from '@/types/PageRequest'
import { seriesMetadataToDto } from '@/functions/series'

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

export const useRefreshMetadataBook = defineMutation(() =>
  useMutation({
    mutation: (bookId: string) =>
      komgaClient.POST('/api/v1/books/{bookId}/metadata/refresh', {
        params: {
          path: {
            bookId: bookId,
          },
        },
      }),
  }),
)

export const useAnalyzeBook = defineMutation(() =>
  useMutation({
    mutation: (bookId: string) =>
      komgaClient.POST('/api/v1/books/{bookId}/analyze', {
        params: {
          path: {
            bookId: bookId,
          },
        },
      }),
  }),
)

export const useMarkBookRead = defineMutation(() =>
  useMutation({
    mutation: (bookId: string) =>
      komgaClient.PATCH('/api/v1/books/{bookId}/read-progress', {
        params: {
          path: {
            bookId: bookId,
          },
        },
        body: { completed: true },
      }),
  }),
)

export const useMarkBookUnread = defineMutation(() =>
  useMutation({
    mutation: (bookId: string) =>
      komgaClient.DELETE('/api/v1/books/{bookId}/read-progress', {
        params: {
          path: {
            bookId: bookId,
          },
        },
      }),
  }),
)

export const useDeleteBook = defineMutation(() =>
  useMutation({
    mutation: (bookId: string) =>
      komgaClient.DELETE('/api/v1/books/{bookId}/file', {
        params: {
          path: {
            bookId: bookId,
          },
        },
      }),
  }),
)

export const useUpdateBookMetadata = defineMutation(() => {
  // const queryCache = useQueryCache()
  return useMutation({
    mutation: ({
      bookId,
      metadata,
    }: {
      bookId: string
      metadata: components['schemas']['BookMetadataDto']
    }) =>
      komgaClient.PATCH('/api/v1/books/{bookId}/metadata', {
        params: {
          path: {
            bookId: bookId,
          },
        },
        body: metadata,
      }),
    onSuccess: () => {
      //TODO: check how to invalidate cache
      // void queryCache.invalidateQueries({ key: QUERY_KEYS_LIBRARIES.root })
    },
  })
})
