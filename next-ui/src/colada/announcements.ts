import { defineMutation, defineQuery, useMutation, useQuery, useQueryCache } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'

export const QUERY_KEYS_ANNOUNCEMENTS = {
  root: ['announcements'] as const,
}

export const useAnnouncements = defineQuery(() => {
  const { data, ...rest } = useQuery({
    key: () => QUERY_KEYS_ANNOUNCEMENTS.root,
    query: () =>
      komgaClient
        .GET('/api/v1/announcements')
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
    // 1 hour
    staleTime: 60 * 60 * 1000,
    gcTime: false,
  })

  const unreadCount = computed(
    () => data.value?.items?.filter((x) => false == x._komga?.read)?.length || 0,
  )

  return { ...rest, data, unreadCount }
})
export const useMarkAnnouncementsRead = defineMutation(() => {
  const queryCache = useQueryCache()
  return useMutation({
    mutation: (announcementIds: string[]) =>
      komgaClient.PUT('/api/v1/announcements', { body: announcementIds }),
    onSuccess: () => {
      void queryCache.invalidateQueries({ key: QUERY_KEYS_ANNOUNCEMENTS.root })
    },
  })
})
