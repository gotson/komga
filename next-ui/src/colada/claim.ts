import { defineMutation, defineQuery, useMutation, useQuery, useQueryCache } from '@pinia/colada'
import { komgaClaimServer, komgaGetClaimStatus } from '@/generated/openapi'

export const QUERY_KEYS_CLAIM = {
  root: ['claim'] as const,
}

export const useClaimStatus = defineQuery(() => {
  return useQuery({
    key: () => QUERY_KEYS_CLAIM.root,
    query: () => komgaGetClaimStatus(),
    // forever
    staleTime: 0,
    gcTime: false,
  })
})

export const useClaimServer = defineMutation(() => {
  const queryCache = useQueryCache()
  return useMutation({
    mutation: ({ username, password }: { username: string; password: string }) =>
      komgaClaimServer({
        headers: {
          'X-Komga-Email': username,
          'X-Komga-Password': password,
        },
      }),
    onSuccess: () => void queryCache.invalidateQueries({ key: QUERY_KEYS_CLAIM.root }),
  })
})
