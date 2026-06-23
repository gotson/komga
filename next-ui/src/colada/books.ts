import {
  defineInfiniteQueryOptions,
  defineMutation,
  defineQueryOptions,
  useMutation,
} from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'
import type { components } from '@/generated/openapi/komga'
import { PageRequest, type Sort, sortToString } from '@/types/PageRequest'
import { entityChanged } from '@/colada/cache'
import { useAppStore } from '@/stores/app'

export const QUERY_KEYS_BOOKS = {
  root: ['books'] as const,
  bySearch: (request: object) => [...QUERY_KEYS_BOOKS.root, JSON.stringify(request)] as const,
  byId: (bookId: string) => [...QUERY_KEYS_BOOKS.root, bookId] as const,
}

export const bookListQuery = defineQueryOptions(
  ({
    search,
    pageRequest,
  }: {
    search: components['schemas']['BookSearch']
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
    placeholderData: (previousData) => previousData,
  }),
)

export const bookListQueryInfinite = defineInfiniteQueryOptions(
  ({ search, sort }: { search: components['schemas']['BookSearch']; sort?: Sort[] }) => ({
    key: QUERY_KEYS_BOOKS.bySearch({ search: search, sort: sort, infinite: true }),
    initialPageParam: new PageRequest(0, 50, sort),
    query: ({ pageParam }) =>
      komgaClient
        .POST('/api/v1/books/list', {
          body: search,
          params: {
            query: {
              page: pageParam.page,
              size: pageParam.size,
              sort: sort?.map((it) => sortToString(it)),
            },
          },
        })
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
    getNextPageParam: (lastPage, _, lastPageParam) =>
      !lastPage?.last ? lastPageParam.next() : null,
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

export const useMarkBookRead = defineMutation(() => {
  const appStore = useAppStore()
  return useMutation({
    mutation: (bookId: string) =>
      komgaClient.PATCH('/api/v1/books/{bookId}/read-progress', {
        params: {
          path: {
            bookId: bookId,
          },
        },
        body: { completed: true },
      }),
    onSuccess: (data, bookId) => {
      if (appStore.sseUnavailable) entityChanged(QUERY_KEYS_BOOKS.root, bookId)
    },
  })
})

export const useMarkBookUnread = defineMutation(() => {
  const appStore = useAppStore()
  return useMutation({
    mutation: (bookId: string) =>
      komgaClient.DELETE('/api/v1/books/{bookId}/read-progress', {
        params: {
          path: {
            bookId: bookId,
          },
        },
      }),
    onSuccess: (data, bookId) => {
      if (appStore.sseUnavailable) entityChanged(QUERY_KEYS_BOOKS.root, bookId)
    },
  })
})

export const useDeleteBook = defineMutation(() => {
  const appStore = useAppStore()
  return useMutation({
    mutation: (bookId: string) =>
      komgaClient.DELETE('/api/v1/books/{bookId}/file', {
        params: {
          path: {
            bookId: bookId,
          },
        },
      }),
    onSuccess: (data, bookId) => {
      if (appStore.sseUnavailable) entityChanged(QUERY_KEYS_BOOKS.root, bookId)
    },
  })
})

export const useUpdateBookMetadata = defineMutation(() => {
  const appStore = useAppStore()
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
    onSuccess: (data, { bookId }) => {
      if (appStore.sseUnavailable) entityChanged(QUERY_KEYS_BOOKS.root, bookId)
    },
  })
})
