import { defineMutation, defineQuery, useMutation, useQuery, useQueryCache } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'
import type { components } from '@/generated/openapi/komga'

export const QUERY_KEYS_SETTINGS = {
  root: ['settings'] as const,
}

export const useSettings = defineQuery(() => {
  return useQuery({
    key: () => QUERY_KEYS_SETTINGS.root,
    query: () =>
      komgaClient
        .GET('/api/v1/settings')
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
    // 1 hour
    staleTime: 60 * 60 * 1000,
    gcTime: false,
  })
})

export const useUpdateSettings = defineMutation(() => {
  const queryCache = useQueryCache()
  return useMutation({
    mutation: (settings: components['schemas']['SettingsUpdateDto']) =>
      komgaClient.PATCH('/api/v1/settings', {
        body: settings,
      }),
    onSuccess: () => {
      void queryCache.invalidateQueries({ key: QUERY_KEYS_SETTINGS.root })
    },
  })
})
