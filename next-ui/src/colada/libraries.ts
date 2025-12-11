import { defineMutation, defineQuery, useMutation, useQuery, useQueryCache } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'
import { useClientSettingsUser } from '@/colada/client-settings'
import { combinePromises } from '@/colada/utils'
import type { components } from '@/generated/openapi/komga'

export const QUERY_KEYS_LIBRARIES = {
  root: ['libraries'] as const,
}

export const useLibraries = defineQuery(() => {
  const {
    data,
    refresh: refreshLibraries,
    refetch: refetchLibraries,
    ...rest
  } = useQuery({
    key: () => QUERY_KEYS_LIBRARIES.root,
    query: () =>
      komgaClient
        .GET('/api/v1/libraries')
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
    // 1 hour
    staleTime: 60 * 60 * 1000,
    gcTime: false,
  })

  const {
    userLibraries,
    refresh: refreshSettings,
    refetch: refetchSettings,
  } = useClientSettingsUser()

  const refresh = combinePromises(refreshLibraries, [refreshSettings])
  const refetch = combinePromises(refetchLibraries, [refetchSettings])

  const ordered = computed(() =>
    data?.value?.sort(
      (a, b) =>
        (userLibraries.value?.[a.id]?.order || 0) - (userLibraries.value?.[b.id]?.order || 0),
    ),
  )

  const unpinned = computed(
    () => ordered.value?.filter((it) => userLibraries.value?.[it.id]?.unpinned) || [],
  )
  const pinned = computed(
    () => ordered.value?.filter((it) => !userLibraries.value?.[it.id]?.unpinned) || [],
  )

  return {
    data,
    ordered,
    unpinned,
    pinned,
    refresh,
    refetch,
    ...rest,
  }
})

export const useCreateLibrary = defineMutation(() => {
  const queryCache = useQueryCache()
  return useMutation({
    mutation: (library: components['schemas']['LibraryCreationDto']) =>
      komgaClient.POST('/api/v1/libraries', {
        body: library,
      }),
    onSuccess: () => {
      void queryCache.invalidateQueries({ key: QUERY_KEYS_LIBRARIES.root })
    },
  })
})

export const useUpdateLibrary = defineMutation(() => {
  const queryCache = useQueryCache()
  return useMutation({
    mutation: (library: components['schemas']['LibraryDto']) =>
      komgaClient.PATCH('/api/v1/libraries/{libraryId}', {
        params: {
          path: {
            libraryId: library.id,
          },
        },
        body: library,
      }),
    onSuccess: () => {
      void queryCache.invalidateQueries({ key: QUERY_KEYS_LIBRARIES.root })
    },
  })
})
