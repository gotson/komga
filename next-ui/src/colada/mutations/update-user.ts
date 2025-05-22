import {defineMutation, useMutation, useQueryCache} from '@pinia/colada'
import {komgaClient} from '@/api/komga-client'
import type {components} from '@/generated/openapi/komga'

export const useUpdateUser = defineMutation(() => {
  const queryCache = useQueryCache()
  return useMutation({
    mutation: (user: components['schemas']['UserDto']) =>
      komgaClient.PATCH('/api/v2/users/{id}', {
        params: {path: {id: user.id}},
        body: user,
      }),
    onSuccess: () => {
      queryCache.invalidateQueries({key: ['users']})
    },
    onError: (error) => {
      console.log('update user error', error)
    },
  })
})
