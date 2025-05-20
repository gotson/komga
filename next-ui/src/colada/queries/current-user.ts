import {useQuery} from '@pinia/colada'
import {komgaClient} from '@/api/komga-client'

export function useCurrentUser() {
  return useQuery({
    key: () => ['current-user'],
    query: () => komgaClient.GET('/api/v2/users/me')
      // unwrap the openapi-fetch structure on success
      .then((res) => res.data),
    // 10 minutes
    staleTime: 10 * 60 * 1000,
    gcTime: false,
    autoRefetch: true,
  })
}
