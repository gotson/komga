import {defineQuery, useQuery} from '@pinia/colada'
import {komgaClient} from '@/api/komga-client'

export const useAnnouncements = defineQuery(() => {
  const {data, ...rest} = useQuery({
    key: () => ['announcements'],
    query: () => komgaClient.GET('/api/v1/announcements')
      // unwrap the openapi-fetch structure on success
      .then((res) => res.data),
    // 1 hour
    staleTime: 60 * 60 * 1000,
    gcTime: false,
  })

  const unreadCount = computed(()  => data.value?.items
    ?.filter((x) => false == x._komga?.read)
    ?.length || 0
  )

  return {...rest, data, unreadCount}
})
