import { defineQuery, useQuery } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'
import { useClientSettingsUser } from '@/colada/client-settings'

export const useLibraries = defineQuery(() => {
  const { data, ...rest } = useQuery({
    key: () => ['libraries'],
    query: () =>
      komgaClient
        .GET('/api/v1/libraries')
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
    // 1 hour
    staleTime: 60 * 60 * 1000,
    gcTime: false,
  })

  const { userLibraries } = useClientSettingsUser()

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
    ...rest,
  }
})
