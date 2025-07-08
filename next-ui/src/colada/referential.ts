import { defineQuery, useQuery } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'

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
