import { defineMutation, defineQuery, useMutation, useQuery, useQueryCache } from '@pinia/colada'
import { komgaGetAnnouncements, komgaMarkAnnouncementsRead } from '@/generated/openapi'

export const QUERY_KEYS_ANNOUNCEMENTS = {
  root: ['announcements'] as const,
}

export const useAnnouncements = defineQuery(() => {
  const { data, ...rest } = useQuery({
    key: () => QUERY_KEYS_ANNOUNCEMENTS.root,
    query: () => komgaGetAnnouncements(),
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
    mutation: (announcementIds: string[]) => komgaMarkAnnouncementsRead({ body: announcementIds }),
    onSuccess: () => void queryCache.invalidateQueries({ key: QUERY_KEYS_ANNOUNCEMENTS.root }),
  })
})
