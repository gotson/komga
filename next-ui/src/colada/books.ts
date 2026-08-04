import {
  defineInfiniteQueryOptions,
  defineMutation,
  defineQueryOptions,
  useMutation,
} from '@pinia/colada'
import { PageRequest, type Sort, sortToString } from '@/types/PageRequest'
import { entityChanged } from '@/colada/cache'
import { useAppStore } from '@/stores/app'
import {
  type BookMetadataDto,
  type BookSearch,
  komgaBookAnalyze,
  komgaBookRefreshMetadata,
  komgaDeleteBookFile,
  komgaDeleteBookReadProgress,
  komgaGetBookById,
  komgaGetBooks,
  komgaGetBooksOnDeck,
  komgaMarkBookReadProgress,
  komgaUpdateBookMetadata,
} from '@/generated/openapi'

export const QUERY_KEYS_BOOKS = {
  root: ['books'] as const,
  bySearch: (request: object) => [...QUERY_KEYS_BOOKS.root, JSON.stringify(request)] as const,
  byId: (bookId: string) => [...QUERY_KEYS_BOOKS.root, bookId] as const,
  onDeck: (request: object) => [...QUERY_KEYS_BOOKS.root, 'deck', JSON.stringify(request)] as const,
}

export const bookListQuery = defineQueryOptions(
  ({ search, pageRequest }: { search: BookSearch; pageRequest?: PageRequest }) => ({
    key: QUERY_KEYS_BOOKS.bySearch({ search: search, pageRequest: pageRequest }),
    query: () =>
      komgaGetBooks({
        body: search,
        query: {
          ...pageRequest,
        },
      }),
    placeholderData: (previousData) => previousData,
  }),
)

export const bookListQueryInfinite = defineInfiniteQueryOptions(
  ({ search, sort }: { search: BookSearch; sort?: Sort[] }) => ({
    key: QUERY_KEYS_BOOKS.bySearch({ search: search, sort: sort, infinite: true }),
    initialPageParam: new PageRequest(0, 50, sort),
    query: ({ pageParam }) =>
      komgaGetBooks({
        body: search,
        query: {
          page: pageParam.page,
          size: pageParam.size,
          sort: sort?.map((it) => sortToString(it)),
        },
      }),
    getNextPageParam: (lastPage, _, lastPageParam) =>
      !lastPage?.last ? lastPageParam.next() : null,
  }),
)

export const booksOnDeckQueryInfinite = defineInfiniteQueryOptions(
  ({ libraryIds }: { libraryIds?: string[] }) => ({
    key: QUERY_KEYS_BOOKS.onDeck({ libraryIds: libraryIds, infinite: true }),
    initialPageParam: new PageRequest(0, 50),
    query: ({ pageParam }) =>
      komgaGetBooksOnDeck({
        query: {
          library_id: libraryIds,
          page: pageParam.page,
          size: pageParam.size,
        },
      }),
    getNextPageParam: (lastPage, _, lastPageParam) =>
      !lastPage?.last ? lastPageParam.next() : null,
  }),
)

export const bookDetailQuery = defineQueryOptions(({ bookId }: { bookId: string }) => ({
  key: QUERY_KEYS_BOOKS.byId(bookId),
  query: () =>
    komgaGetBookById({
      path: {
        bookId: bookId,
      },
    }),
}))

export const useRefreshMetadataBook = defineMutation(() =>
  useMutation({
    mutation: (bookId: string) =>
      komgaBookRefreshMetadata({
        path: {
          bookId: bookId,
        },
      }),
  }),
)

export const useAnalyzeBook = defineMutation(() =>
  useMutation({
    mutation: (bookId: string) =>
      komgaBookAnalyze({
        path: {
          bookId: bookId,
        },
      }),
  }),
)

export const useMarkBookRead = defineMutation(() => {
  const appStore = useAppStore()
  return useMutation({
    mutation: (bookId: string) =>
      komgaMarkBookReadProgress({
        path: {
          bookId: bookId,
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
      komgaDeleteBookReadProgress({
        path: {
          bookId: bookId,
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
      komgaDeleteBookFile({
        path: {
          bookId: bookId,
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
    mutation: ({ bookId, metadata }: { bookId: string; metadata: BookMetadataDto }) =>
      komgaUpdateBookMetadata({
        path: {
          bookId: bookId,
        },
        body: metadata,
      }),
    onSuccess: (data, { bookId }) => {
      if (appStore.sseUnavailable) entityChanged(QUERY_KEYS_BOOKS.root, bookId)
    },
  })
})
