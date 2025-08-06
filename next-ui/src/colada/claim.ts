import { defineMutation, defineQuery, useMutation, useQuery, useQueryCache } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'

export const QUERY_KEYS_CLAIM = {
  root: ['claim'] as const,
}

export const useClaimStatus = defineQuery(() => {
  return useQuery({
    key: () => QUERY_KEYS_CLAIM.root,
    query: () =>
      komgaClient
        .GET('/api/v1/claim')
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
    // forever
    staleTime: 0,
    gcTime: false,
  })
})

export const useClaimServer = defineMutation(() => {
  const queryCache = useQueryCache()
  return useMutation({
    mutation: ({ username, password }: { username: string; password: string }) =>
      komgaClient.POST('/api/v1/claim', {
        params: {
          header: {
            'X-Komga-Email': username,
            'X-Komga-Password': password,
          },
        },
      }),
    onSuccess: () => {
      queryCache.cancelQueries({ key: QUERY_KEYS_CLAIM.root })
    },
  })
})
