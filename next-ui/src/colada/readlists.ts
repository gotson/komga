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
    pause = false,
    pageRequest,
  }: {
    search?: string
    libraryIds?: string[]
    pause?: boolean
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
    enabled: !pause,
    placeholderData: (previousData: any) => previousData, // eslint-disable-line @typescript-eslint/no-explicit-any
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
