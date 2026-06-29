import {
  defineMutation,
  defineQuery,
  defineQueryOptions,
  useMutation,
  useQuery,
  useQueryCache,
} from '@pinia/colada'
import { UserRoles } from '@/types/UserRoles'
import { QUERY_KEYS_CLIENT_SETTINGS } from '@/colada/client-settings'
import { useAppStore } from '@/stores/app'
import { invalidateAll } from '@/colada/cache'
import {
  type ApiKeyRequestDto,
  komgaAddUser,
  komgaCreateApiKeyForCurrentUser,
  komgaDeleteApiKeyByKeyId,
  komgaDeleteUserById,
  komgaGetApiKeysForCurrentUser,
  komgaGetAuthenticationActivity,
  komgaGetAuthenticationActivityForCurrentUser,
  komgaGetCurrentUser,
  komgaGetUsers,
  komgaPostLogout1,
  komgaUpdatePasswordByUserId,
  komgaUpdateUserById,
  type UserCreationDto,
  type UserDto,
} from '@/generated/openapi'

export const QUERY_KEYS_USERS = {
  root: ['users'] as const,
  currentUser: () => [...QUERY_KEYS_USERS.root, 'current-user'] as const,
  apiKeys: () => [...QUERY_KEYS_USERS.currentUser(), 'api-keys'] as const,
}

export const useUsers = defineQuery(() =>
  useQuery({
    key: () => QUERY_KEYS_USERS.root,
    query: () => komgaGetUsers(),
  }),
)

export const useCurrentUser = defineQuery(() => {
  const { data, error, ...rest } = useQuery({
    key: QUERY_KEYS_USERS.currentUser,
    query: () => komgaGetCurrentUser(),
    // 10 minutes
    staleTime: 10 * 60 * 1000,
    gcTime: false,
    autoRefetch: true,
    meta: {
      no401handling: true,
    },
  })

  const isAuthenticated = computed(() => !!data.value && !error.value)
  const hasRole = (role: UserRoles) => data.value?.roles.includes(role)
  const isAdmin = computed(() => hasRole(UserRoles.ADMIN))

  return {
    data,
    error,
    ...rest,
    hasRole,
    isAdmin,
    isAuthenticated,
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
      komgaGetCurrentUser({
        headers: {
          authorization: 'Basic ' + btoa(username + ':' + password),
          'X-Requested-With': 'XMLHttpRequest',
        },
        query: {
          'remember-me': rememberMe,
        },
      }),
    onSuccess: (data) => {
      queryCache.setQueryData(QUERY_KEYS_USERS.currentUser(), data)
      queryCache.cancelQueries({ key: QUERY_KEYS_USERS.currentUser() })

      void queryCache.invalidateQueries({ key: QUERY_KEYS_CLIENT_SETTINGS.root })
    },
  })
})

export const useLogout = defineMutation(() =>
  useMutation({
    mutation: () => komgaPostLogout1(),
    onSuccess: () => {
      userLoggedOut()
    },
  }),
)

export const useCreateUser = defineMutation(() =>
  useMutation({
    mutation: (user: UserCreationDto) =>
      komgaAddUser({
        body: user,
      }),
    onSuccess: () => userChanged(),
  }),
)

export const useUpdateUser = defineMutation(() =>
  useMutation({
    mutation: (user: UserDto) =>
      komgaUpdateUserById({
        path: { id: user.id },
        body: user,
      }),
    onSuccess: () => userChanged(),
  }),
)

export const useUpdateUserPassword = defineMutation(() =>
  useMutation({
    mutation: ({ userId, newPassword }: { userId: string; newPassword: string }) =>
      komgaUpdatePasswordByUserId({
        path: { id: userId },
        body: {
          password: newPassword,
        },
      }),
  }),
)

export const useDeleteUser = defineMutation(() =>
  useMutation({
    mutation: (userId: string) =>
      komgaDeleteUserById({
        path: { id: userId },
      }),
    onSuccess: () => userChanged(),
  }),
)

///////////
// API KEYS
///////////

export const useApiKeys = defineQuery(() =>
  useQuery({
    key: QUERY_KEYS_USERS.apiKeys,
    query: () => komgaGetApiKeysForCurrentUser(),
  }),
)

export const useCreateApiKey = defineMutation(() => {
  const queryCache = useQueryCache()
  return useMutation({
    mutation: (apiKey: ApiKeyRequestDto) =>
      komgaCreateApiKeyForCurrentUser({
        body: apiKey,
      }),
    onSuccess: () => void queryCache.invalidateQueries({ key: QUERY_KEYS_USERS.apiKeys() }),
  })
})

export const useDeleteApiKey = defineMutation(() => {
  const queryCache = useQueryCache()
  return useMutation({
    mutation: (keyId: string) =>
      komgaDeleteApiKeyByKeyId({
        path: { keyId: keyId },
      }),
    onSuccess: () => void queryCache.invalidateQueries({ key: QUERY_KEYS_USERS.apiKeys() }),
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
    key: [
      ...QUERY_KEYS_USERS.root,
      'authentication-activity',
      { page: page, size: size, sort: sort, unpaged: unpaged },
    ],
    query: () =>
      komgaGetAuthenticationActivity({
        query: { page: page, size: size, sort: sort, unpaged: unpaged },
      }),
    placeholderData: (previousData) => previousData,
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
      ...QUERY_KEYS_USERS.currentUser(),
      'authentication-activity',
      { page: page, size: size, sort: sort, unpaged: unpaged },
    ],
    query: () =>
      komgaGetAuthenticationActivityForCurrentUser({
        query: { page: page, size: size, sort: sort, unpaged: unpaged },
      }),
    placeholderData: (previousData) => previousData,
  }),
)

function userChanged() {
  const appStore = useAppStore()
  if (appStore.sseUnavailable) {
    const queryCache = useQueryCache()
    void queryCache.invalidateQueries({ key: QUERY_KEYS_USERS.root })
  }
}

export function userLoggedOut() {
  invalidateAll()
}
