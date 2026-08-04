import {
  defineInfiniteQueryOptions,
  defineMutation,
  defineQueryOptions,
  useMutation,
} from '@pinia/colada'
import { PageRequest } from '@/types/PageRequest'
import { entityChanged } from '@/colada/cache'
import { useAppStore } from '@/stores/app'
import {
  komgaDeleteCollectionById,
  komgaGetCollectionById,
  komgaGetCollections,
  komgaUpdateCollectionById,
  type CollectionUpdateDto,
} from '@/generated/openapi'

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
      komgaGetCollections({
        query: {
          search: search,
          library_id: libraryIds,
          ...pageRequest,
        },
      }),
    placeholderData: (previousData) => previousData,
  }),
)

export const collectionsListQueryInfinite = defineInfiniteQueryOptions(
  ({ libraryIds }: { libraryIds?: string[] }) => ({
    key: QUERY_KEYS_COLLECTIONS.bySearch({ libraryIds, infinite: true }),
    initialPageParam: new PageRequest(0, 50),
    query: ({ pageParam }) =>
      komgaGetCollections({
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

export const collectionDetailQuery = defineQueryOptions(
  ({ collectionId }: { collectionId: string }) => ({
    key: QUERY_KEYS_COLLECTIONS.byId(collectionId),
    query: () =>
      komgaGetCollectionById({
        path: {
          id: collectionId,
        },
      }),
  }),
)

export const useUpdateCollection = defineMutation(() => {
  const appStore = useAppStore()
  return useMutation({
    mutation: ({ collectionId, data }: { collectionId: string; data: CollectionUpdateDto }) =>
      komgaUpdateCollectionById({
        path: {
          id: collectionId,
        },
        body: data,
      }),
    onSuccess: (_data, { collectionId }) => {
      if (appStore.sseUnavailable) entityChanged(QUERY_KEYS_COLLECTIONS.root, collectionId)
    },
  })
})

export const useDeleteCollection = defineMutation(() => {
  const appStore = useAppStore()
  return useMutation({
    mutation: (collectionId: string) =>
      komgaDeleteCollectionById({
        path: {
          id: collectionId,
        },
      }),
    onSuccess: (_data, collectionId) => {
      if (appStore.sseUnavailable) entityChanged(QUERY_KEYS_COLLECTIONS.root, collectionId)
    },
  })
})
