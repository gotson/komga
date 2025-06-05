import { defineMutation, useMutation, useQueryCache } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'

export const useMarkAnnouncementsRead = defineMutation(() => {
  const queryCache = useQueryCache()
  return useMutation({
    mutation: (announcementIds: string[]) =>
      komgaClient.PUT('/api/v1/announcements', { body: announcementIds }),
    onSuccess: () => {
      void queryCache.invalidateQueries({ key: ['announcements'] })
    },
    onError: (error) => {
      console.log('announcements mark read error', error)
    },
  })
})
