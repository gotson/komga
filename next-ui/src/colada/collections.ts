import { defineMutation, defineQueryOptions, useMutation } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'
import type { PageRequest } from '@/types/PageRequest'
import type { components } from '@/generated/openapi/komga'

export const QUERY_KEYS_COLLECTIONS = {
  root: ['collections'] as const,
  bySearch: (request: object) => [...QUERY_KEYS_COLLECTIONS.root, JSON.stringify(request)] as const,
  byId: (seriesId: string) => [...QUERY_KEYS_COLLECTIONS.root, seriesId] as const,
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
  // const queryCache = useQueryCache()
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
    onSuccess: () => {
      //TODO: check how to invalidate cache
      // void queryCache.invalidateQueries({ key: QUERY_KEYS_LIBRARIES.root })
    },
  })
})

export const useDeleteCollection = defineMutation(() =>
  useMutation({
    mutation: (collectionId: string) =>
      komgaClient.DELETE('/api/v1/collections/{id}', {
        params: {
          path: {
            id: collectionId,
          },
        },
      }),
  }),
)
