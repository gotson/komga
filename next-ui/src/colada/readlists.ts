import { defineMutation, defineQueryOptions, useMutation } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'
import type { components } from '@/generated/openapi/komga'
import type { PageRequest } from '@/types/PageRequest'

export const QUERY_KEYS_READLIST = {
  root: ['readlists'] as const,
  bySearch: (request: object) => [...QUERY_KEYS_READLIST.root, JSON.stringify(request)] as const,
}

export const useListReadLists = defineQueryOptions(
  ({
    search,
    libraryId,
    pageRequest,
  }: {
    search?: string
    libraryId?: string
    pageRequest?: PageRequest
  }) => ({
    key: QUERY_KEYS_READLIST.bySearch({
      search: search,
      libraryId: libraryId,
      pageRequest: pageRequest,
    }),
    query: () =>
      komgaClient
        .GET('/api/v1/readlists', {
          params: {
            query: {
              search: search,
              libraryId: libraryId,
              ...pageRequest,
            },
          },
        })
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
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
