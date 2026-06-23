import {
  defineInfiniteQueryOptions,
  defineMutation,
  defineQueryOptions,
  useMutation,
} from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'
import { PageRequest } from '@/types/PageRequest'
import type { components } from '@/generated/openapi/komga'
import { entityChanged } from '@/colada/cache'
import { useAppStore } from '@/stores/app'

export const QUERY_KEYS_COLLECTIONS = {
  root: ['collections'] as const,
  bySearch: (request: object) => [...QUERY_KEYS_COLLECTIONS.root, JSON.stringify(request)] as const,
  byId: (id: string) => [...QUERY_KEYS_COLLECTIONS.root, id] as const,
}

export const collectionsListQuery = defineQueryOptions(
  ({
    search,
    libraryIds,
    pageRequest,
  }: {
    search?: string
    libraryIds?: string[]
    pageRequest?: PageRequest
  }) => ({
    key: QUERY_KEYS_COLLECTIONS.bySearch({ search: search, libraryIds, pageRequest: pageRequest }),
    query: () =>
      komgaClient
        .GET('/api/v1/collections', {
          params: {
            query: {
              search: search,
              library_id: libraryIds,
              ...pageRequest,
            },
          },
        })
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
    placeholderData: (previousData) => previousData,
  }),
)

export const collectionsListQueryInfinite = defineInfiniteQueryOptions(
  ({ libraryIds }: { libraryIds?: string[] }) => ({
    key: QUERY_KEYS_COLLECTIONS.bySearch({ libraryIds, infinite: true }),
    initialPageParam: new PageRequest(0, 50),
    query: ({ pageParam }) =>
      komgaClient
        .GET('/api/v1/collections', {
          params: {
            query: {
              library_id: libraryIds,
              page: pageParam.page,
              size: pageParam.size,
            },
          },
        })
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
    getNextPageParam: (lastPage, _, lastPageParam) =>
      !lastPage?.last ? lastPageParam.next() : null,
  }),
)

export const collectionDetailQuery = defineQueryOptions(
  ({ collectionId }: { collectionId: string }) => ({
    key: QUERY_KEYS_COLLECTIONS.byId(collectionId),
    query: () =>
      komgaClient
        .GET('/api/v1/collections/{id}', {
          params: {
            path: {
              id: collectionId,
            },
          },
        })
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
  }),
)

export const useUpdateCollection = defineMutation(() => {
  const appStore = useAppStore()
  return useMutation({
    mutation: ({
      collectionId,
      data,
    }: {
      collectionId: string
      data: components['schemas']['CollectionUpdateDto']
    }) =>
      komgaClient.PATCH('/api/v1/collections/{id}', {
        params: {
          path: {
            id: collectionId,
          },
        },
        body: data,
      }),
    onSuccess: (data, { collectionId }) => {
      if (appStore.sseUnavailable) entityChanged(QUERY_KEYS_COLLECTIONS.root, collectionId)
    },
  })
})

export const useDeleteCollection = defineMutation(() => {
  const appStore = useAppStore()
  return useMutation({
    mutation: (collectionId: string) =>
      komgaClient.DELETE('/api/v1/collections/{id}', {
        params: {
          path: {
            id: collectionId,
          },
        },
      }),
    onSuccess: (data, collectionId) => {
      if (appStore.sseUnavailable) entityChanged(QUERY_KEYS_COLLECTIONS.root, collectionId)
    },
  })
})
