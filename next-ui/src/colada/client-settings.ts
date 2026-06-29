import { defineMutation, defineQuery, useMutation, useQuery, useQueryCache } from '@pinia/colada'
import { CLIENT_SETTING_USER, type ClientSettingUserLibrary } from '@/types/ClientSettingsUser'
import {
  type ClientSettingUserUpdateDto,
  komgaGetUserSettings,
  komgaSaveUserSetting,
} from '@/generated/openapi'

export const QUERY_KEYS_CLIENT_SETTINGS = {
  root: ['client-settings'] as const,
  global: () => [...QUERY_KEYS_CLIENT_SETTINGS.root, 'global'] as const,
  user: () => [...QUERY_KEYS_CLIENT_SETTINGS.root, 'user'] as const,
}

export const useClientSettingsUser = defineQuery(() => {
  const { data, ...rest } = useQuery({
    key: () => QUERY_KEYS_CLIENT_SETTINGS.user(),
    query: () => komgaGetUserSettings(),
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
    mutation: (settings: Record<CLIENT_SETTING_USER, ClientSettingUserUpdateDto>) =>
      komgaSaveUserSetting({
        body: settings,
      }),
    onSuccess: () => {
      void queryCache.invalidateQueries({ key: QUERY_KEYS_CLIENT_SETTINGS.user() })
    },
  })
})
