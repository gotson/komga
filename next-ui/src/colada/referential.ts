import { defineQuery, defineQueryOptions, useQuery } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'
import type { PageRequest } from '@/types/PageRequest'

export const useSharingLabels = defineQuery(() => {
  return useQuery({
    key: () => ['sharing-labels'],
    query: () =>
      komgaClient
        .GET('/api/v1/sharing-labels')
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
    // 1 hour
    staleTime: 60 * 60 * 1000,
    gcTime: false,
  })
})

export const authorsQuery = defineQueryOptions(
  ({
    search,
    role,
    library_id,
    collection_id,
    series_id,
    readlist_id,
    pageRequest,
    pause = false,
    placeholder = true,
  }: {
    search?: string
    role?: string
    library_id?: string[]
    collection_id?: string
    series_id?: string
    readlist_id?: string
    pageRequest?: PageRequest
    pause?: boolean
    placeholder?: boolean
  }) => {
    const queryParams = {
      search: search,
      role: role,
      library_id: library_id,
      collection_id: collection_id,
      series_id: series_id,
      readlist_id: readlist_id,
      ...pageRequest,
    }
    return {
      key: ['authors', queryParams],
      query: () =>
        komgaClient
          .GET('/api/v2/authors', {
            params: {
              query: queryParams,
            },
          })
          // unwrap the openapi-fetch structure on success
          .then((res) => res.data),
      enabled: !pause,
      placeholderData: placeholder ? (previousData: any) => previousData : undefined, // eslint-disable-line @typescript-eslint/no-explicit-any
    }
  },
)

export const authorRolesQuery = defineQueryOptions(() => {
  return {
    key: ['authors', 'roles'],
    query: () =>
      komgaClient
        .GET('/api/v1/authors/roles')
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
    placeholderData: (previousData: any) => previousData, // eslint-disable-line @typescript-eslint/no-explicit-any
    staleTime: 60 * 60 * 1000, // 1 hour
  }
})
