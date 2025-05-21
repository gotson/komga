import {defineQuery, useQuery} from '@pinia/colada'
import {komgaClient} from '@/api/komga-client'
import {UserRoles} from '@/types/UserRoles.ts'

export const useCurrentUser = defineQuery(() => {
  const {data, ...rest} = useQuery({
    key: () => ['current-user'],
    query: () => komgaClient.GET('/api/v2/users/me')
      // unwrap the openapi-fetch structure on success
      .then((res) => res.data),
    // 10 minutes
    staleTime: 10 * 60 * 1000,
    gcTime: false,
    autoRefetch: true,
  })

  const hasRole =(role: UserRoles) => data.value?.roles.includes(role)
  const isAdmin = computed(() => hasRole(UserRoles.ADMIN))

  return {
    data,
    ...rest,
    hasRole,
    isAdmin,
  }
})
