import { defineMutation, defineQuery, useMutation, useQuery, useQueryCache } from '@pinia/colada'
import {
  komgaGetServerSettings,
  komgaUpdateServerSettings,
  type SettingsUpdateDto,
} from '@/generated/openapi'

export const QUERY_KEYS_SETTINGS = {
  root: ['settings'] as const,
}

export const useSettings = defineQuery(() => {
  return useQuery({
    key: () => QUERY_KEYS_SETTINGS.root,
    query: () => komgaGetServerSettings(),
    // 1 hour
    staleTime: 60 * 60 * 1000,
    gcTime: false,
  })
})

export const useUpdateSettings = defineMutation(() => {
  const queryCache = useQueryCache()
  return useMutation({
    mutation: (settings: SettingsUpdateDto) =>
      komgaUpdateServerSettings({
        body: settings,
      }),
    onSuccess: () => {
      void queryCache.invalidateQueries({ key: QUERY_KEYS_SETTINGS.root })
    },
  })
})
