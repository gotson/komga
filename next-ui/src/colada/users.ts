import { defineMutation, defineQuery, useMutation, useQuery, useQueryCache } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'
import { UserRoles } from '@/types/UserRoles'
import type { components } from '@/generated/openapi/komga'

export const QUERY_KEYS_USERS = {
  root: ['users'] as const,
  currentUser: ['current-user'] as const,
}

export const useUsers = defineQuery(() => {
  return useQuery({
    key: () => QUERY_KEYS_USERS.root,
    query: () =>
      komgaClient
        .GET('/api/v2/users')
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
  })
})

export const useCurrentUser = defineQuery(() => {
  const { data, ...rest } = useQuery({
    key: () => QUERY_KEYS_USERS.currentUser,
    query: () =>
      komgaClient
        .GET('/api/v2/users/me')
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
    // 10 minutes
    staleTime: 10 * 60 * 1000,
    gcTime: false,
    autoRefetch: true,
  })

  const hasRole = (role: UserRoles) => data.value?.roles.includes(role)
  const isAdmin = computed(() => hasRole(UserRoles.ADMIN))

  return {
    data,
    ...rest,
    hasRole,
    isAdmin,
  }
})

export const useLogout = defineMutation(() => {
  const queryCache = useQueryCache()
  return useMutation({
    mutation: () => komgaClient.POST('/api/logout'),
    onSuccess: () => {
      void queryCache.invalidateQueries({ key: QUERY_KEYS_USERS.currentUser })
    },
  })
})

export const useCreateUser = defineMutation(() => {
  const queryCache = useQueryCache()
  return useMutation({
    mutation: (user: components['schemas']['UserCreationDto']) =>
      komgaClient.POST('/api/v2/users', {
        body: user,
      }),
    onSuccess: () => {
      void queryCache.invalidateQueries({ key: QUERY_KEYS_USERS.root })
    },
  })
})

export const useUpdateUser = defineMutation(() => {
  const queryCache = useQueryCache()
  return useMutation({
    mutation: (user: components['schemas']['UserDto']) =>
      komgaClient.PATCH('/api/v2/users/{id}', {
        params: { path: { id: user.id } },
        body: user,
      }),
    onSuccess: () => {
      void queryCache.invalidateQueries({ key: QUERY_KEYS_USERS.root })
    },
  })
})

export const useUpdateUserPassword = defineMutation(() => {
  return useMutation({
    mutation: ({ userId, newPassword }: { userId: string; newPassword: string }) =>
      komgaClient.PATCH('/api/v2/users/{id}/password', {
        params: { path: { id: userId } },
        body: {
          password: newPassword,
        },
      }),
  })
})

export const useDeleteUser = defineMutation(() => {
  const queryCache = useQueryCache()
  return useMutation({
    mutation: (userId: string) =>
      komgaClient.DELETE('/api/v2/users/{id}', {
        params: { path: { id: userId } },
      }),
    onSuccess: () => {
      void queryCache.invalidateQueries({ key: QUERY_KEYS_USERS.root })
    },
  })
})
