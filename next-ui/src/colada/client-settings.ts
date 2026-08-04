import { defineMutation, defineQuery, useMutation, useQuery, useQueryCache } from '@pinia/colada'
import {
  ClientSettingUser,
  ClientSettingUserSchemas,
  type ClientSettingUserSettings,
} from '@/types/ClientSettingsUser'
import {
  type ClientSettingUserUpdateDto,
  komgaGetUserSettings,
  komgaSaveUserSetting,
} from '@/generated/openapi'
import * as v from 'valibot'

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

  const userSettings = computed(() => {
    const raw = data.value
    const result: Partial<Record<ClientSettingUser, unknown>> = {}

    // only expose settings declared in ClientSettingUser
    for (const settingKey of Object.values(ClientSettingUser)) {
      const schema = ClientSettingUserSchemas[settingKey]
      if (!schema) continue

      // validate raw json against defined schema, else fallback
      const setting = raw?.[settingKey]
      result[settingKey] = setting?.value
        ? v.parse(schema, JSON.parse(setting.value))
        : v.getDefaults(schema)
    }

    return result as ClientSettingUserSettings
  })

  return {
    data,
    ...rest,
    userSettings,
  }
})

export const useUpdateClientSettingsUser = defineMutation(() => {
  const queryCache = useQueryCache()
  return useMutation({
    mutation: (settings: Partial<ClientSettingUserSettings>) => {
      const body: Record<string, ClientSettingUserUpdateDto> = {}
      for (const [key, value] of Object.entries(settings)) {
        if (value !== undefined) {
          body[key] = { value: JSON.stringify(value) }
        }
      }
      return komgaSaveUserSetting({ body })
    },
    onSuccess: () => {
      void queryCache.invalidateQueries({ key: QUERY_KEYS_CLIENT_SETTINGS.user() })
    },
  })
})
