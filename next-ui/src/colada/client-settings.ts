import { defineMutation, defineQuery, useMutation, useQuery, useQueryCache } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'
import type { components } from '@/generated/openapi/komga'
import { CLIENT_SETTING_USER, type ClientSettingUserLibrary } from '@/types/ClientSettingsUser'

export const QUERY_KEYS_CLIENT_SETTINGS = {
  root: ['client-settings'] as const,
  global: () => [...QUERY_KEYS_CLIENT_SETTINGS.root, 'global'] as const,
  user: () => [...QUERY_KEYS_CLIENT_SETTINGS.root, 'user'] as const,
}

export const useClientSettingsUser = defineQuery(() => {
  const { data, ...rest } = useQuery({
    key: () => QUERY_KEYS_CLIENT_SETTINGS.user(),
    query: () =>
      komgaClient
        .GET('/api/v1/client-settings/user/list')
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
    // 1 hour
    staleTime: 60 * 60 * 1000,
    gcTime: false,
  })

  const userLibraries = computed(() => {
    const json = data.value?.[CLIENT_SETTING_USER.NEXTUI_LIBRARIES]?.value
    if (json) return JSON.parse(json) as Record<string, ClientSettingUserLibrary>
    return {}
  })

  return {
    data,
    ...rest,
    userLibraries,
  }
})

export const useUpdateClientSettingsUser = defineMutation(() => {
  const queryCache = useQueryCache()
  return useMutation({
    mutation: (
      settings: Record<CLIENT_SETTING_USER, components['schemas']['ClientSettingUserUpdateDto']>,
    ) =>
      komgaClient.PATCH('/api/v1/client-settings/user', {
        body: settings,
      }),
    onSuccess: () => {
      void queryCache.invalidateQueries({ key: QUERY_KEYS_CLIENT_SETTINGS.user() })
    },
  })
})
