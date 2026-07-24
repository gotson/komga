import { defineMutation, defineQuery, useMutation, useQuery, useQueryCache } from '@pinia/colada'
import {
  type ClientSettingUserOverviewSection,
  ClientSettingUser,
  type ClientSettingUserLibrary,
} from '@/types/ClientSettingsUser'
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

  // a Record mapping a library ID to its settings
  const userLibraries = computed(() => {
    const json = data.value?.[ClientSettingUser.NextUILibraries]?.value
    //TODO: should use valibot for parsing
    if (json) return JSON.parse(json) as Record<string, ClientSettingUserLibrary>
    return {}
  })

  // a Record mapping a libraryId to its overview sections
  const overviewSections = computed(() => {
    const json = data.value?.[ClientSettingUser.NextUIOverviewSections]?.value
    //TODO: should use valibot for parsing
    if (json) return JSON.parse(json) as Record<string, ClientSettingUserOverviewSection[]>
    return {}
  })

  return {
    data,
    ...rest,
    userLibraries,
    overviewSections,
  }
})

export const useUpdateClientSettingsUser = defineMutation(() => {
  const queryCache = useQueryCache()
  return useMutation({
    mutation: (settings: Partial<Record<ClientSettingUser, ClientSettingUserUpdateDto>>) =>
      komgaSaveUserSetting({
        body: settings,
      }),
    onSuccess: () => {
      void queryCache.invalidateQueries({ key: QUERY_KEYS_CLIENT_SETTINGS.user() })
    },
  })
})
