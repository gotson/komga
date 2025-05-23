import {defineMutation, useMutation, useQueryCache} from '@pinia/colada'
import {komgaClient} from '@/api/komga-client'

export const useLogout = defineMutation(() => {
  const queryCache = useQueryCache()
  return useMutation({
    mutation: () =>
      komgaClient.POST('/api/logout'),
    onSuccess: () => {
      void queryCache.invalidateQueries({key: ['current-user']})
    },
    onError: (error) => {
      console.log('logout error', error)
    },
  })
})
