import {
  defineMutation,
  defineQuery,
  defineQueryOptions,
  useMutation,
  useQuery,
  useQueryCache,
} from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'
import { UserRoles } from '@/types/UserRoles'
import type { components } from '@/generated/openapi/komga'

export const QUERY_KEYS_USERS = {
  root: ['users'] as const,
  currentUser: ['current-user'] as const,
  apiKeys: ['current-user', 'api-keys'] as const,
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

export const useLogin = defineMutation(() => {
  const queryCache = useQueryCache()
  return useMutation({
    mutation: ({
      username,
      password,
      rememberMe,
    }: {
      username: string
      password: string
      rememberMe?: boolean
    }) =>
      komgaClient.GET('/api/v2/users/me', {
        headers: {
          authorization: 'Basic ' + btoa(username + ':' + password),
          'X-Requested-With': 'XMLHttpRequest',
        },
        params: {
          query: {
            'remember-me': rememberMe,
          },
        },
      }),
    onSuccess: ({ data }) => {
      queryCache.setQueryData(QUERY_KEYS_USERS.currentUser, data)
      queryCache.cancelQueries({ key: QUERY_KEYS_USERS.currentUser })
    },
  })
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

///////////
// API KEYS
///////////

export const useApiKeys = defineQuery(() => {
  return useQuery({
    key: () => QUERY_KEYS_USERS.apiKeys,
    query: () =>
      komgaClient
        .GET('/api/v2/users/me/api-keys')
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
  })
})

export const useCreateApiKey = defineMutation(() => {
  const queryCache = useQueryCache()
  return useMutation({
    mutation: (apiKey: components['schemas']['ApiKeyRequestDto']) =>
      komgaClient
        .POST('/api/v2/users/me/api-keys', {
          body: apiKey,
        })
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
    onSuccess: () => {
      void queryCache.invalidateQueries({ key: QUERY_KEYS_USERS.apiKeys })
    },
  })
})

export const useDeleteApiKey = defineMutation(() => {
  const queryCache = useQueryCache()
  return useMutation({
    mutation: (keyId: string) =>
      komgaClient.DELETE('/api/v2/users/me/api-keys/{keyId}', {
        params: { path: { keyId: keyId } },
      }),
    onSuccess: () => {
      void queryCache.invalidateQueries({ key: QUERY_KEYS_USERS.apiKeys })
    },
  })
})

///////////
// Authentication Activity
///////////

export const authenticationActivityQuery = defineQueryOptions(
  ({
    page,
    size,
    sort,
    unpaged,
  }: {
    page?: number
    size?: number
    sort?: string[]
    unpaged?: boolean
  }) => ({
    key: ['authentication-activity', { page: page, size: size, sort: sort, unpaged: unpaged }],
    query: () =>
      komgaClient
        .GET('/api/v2/users/authentication-activity', {
          params: {
            query: { page: page, size: size, sort: sort, unpaged: unpaged },
          },
        })
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
    placeholderData: (previousData: any) => previousData, // eslint-disable-line @typescript-eslint/no-explicit-any
  }),
)

export const myAuthenticationActivityQuery = defineQueryOptions(
  ({
    page,
    size,
    sort,
    unpaged,
  }: {
    page?: number
    size?: number
    sort?: string[]
    unpaged?: boolean
  }) => ({
    key: [
      ...QUERY_KEYS_USERS.currentUser,
      'authentication-activity',
      { page: page, size: size, sort: sort, unpaged: unpaged },
    ],
    query: () =>
      komgaClient
        .GET('/api/v2/users/me/authentication-activity', {
          params: {
            query: { page: page, size: size, sort: sort, unpaged: unpaged },
          },
        })
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
    placeholderData: (previousData: any) => previousData, // eslint-disable-line @typescript-eslint/no-explicit-any
  }),
)
