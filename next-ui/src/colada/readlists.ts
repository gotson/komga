import { defineMutation, defineQueryOptions, useMutation } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'
import type { components } from '@/generated/openapi/komga'
import type { PageRequest } from '@/types/PageRequest'

export const QUERY_KEYS_READLIST = {
  root: ['readlists'] as const,
  bySearch: (request: object) => [...QUERY_KEYS_READLIST.root, JSON.stringify(request)] as const,
}

export const readListsListQuery = defineQueryOptions(
  ({
    search,
    libraryIds,
    pageRequest,
  }: {
    search?: string
    libraryIds?: string[]
    pageRequest?: PageRequest
  }) => ({
    key: QUERY_KEYS_READLIST.bySearch({
      search: search,
      libraryIds: libraryIds,
      pageRequest: pageRequest,
    }),
    query: () =>
      komgaClient
        .GET('/api/v1/readlists', {
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

export const useCreateReadList = defineMutation(() => {
  return useMutation({
    mutation: (readList: components['schemas']['ReadListCreationDto']) =>
      komgaClient.POST('/api/v1/readlists', {
        body: readList,
      }),
  })
})

export const useUpdateReadList = defineMutation(() => {
  // const queryCache = useQueryCache()
  return useMutation({
    mutation: ({
      readListId,
      data,
    }: {
      readListId: string
      data: components['schemas']['ReadListUpdateDto']
    }) =>
      komgaClient.PATCH('/api/v1/readlists/{id}', {
        params: {
          path: {
            id: readListId,
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

export const useDeleteReadList = defineMutation(() =>
  useMutation({
    mutation: (readListId: string) =>
      komgaClient.DELETE('/api/v1/readlists/{id}', {
        params: {
          path: {
            id: readListId,
          },
        },
      }),
  }),
)
