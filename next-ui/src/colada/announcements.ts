import { defineMutation, defineQuery, useMutation, useQuery, useQueryCache } from '@pinia/colada'
import { komgaGetAnnouncements, komgaMarkAnnouncementsRead } from '@/generated/openapi'
import { useCurrentUser } from '@/colada/users'
import { STALE_TIME } from '@/types/time'

export const QUERY_KEYS_ANNOUNCEMENTS = {
  root: ['announcements'] as const,
}

export const useAnnouncements = defineQuery(() => {
  const { isAdmin } = useCurrentUser()

  const { data, ...rest } = useQuery({
    key: () => QUERY_KEYS_ANNOUNCEMENTS.root,
    query: () => komgaGetAnnouncements(),
    staleTime: STALE_TIME.LONG,
    gcTime: false,
    enabled: () => isAdmin.value,
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
