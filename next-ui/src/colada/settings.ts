import { defineMutation, defineQuery, useMutation, useQuery, useQueryCache } from '@pinia/colada'
import {
  komgaGetServerSettings,
  komgaUpdateServerSettings,
  type SettingsUpdateDto,
} from '@/generated/openapi'
import { STALE_TIME } from '@/types/time'

export const QUERY_KEYS_SETTINGS = {
  root: ['settings'] as const,
}

export const useSettings = defineQuery(() => {
  return useQuery({
    key: () => QUERY_KEYS_SETTINGS.root,
    query: () => komgaGetServerSettings(),
    staleTime: STALE_TIME.LONG,
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
