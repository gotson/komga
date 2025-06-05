import { defineMutation, useMutation, useQueryCache } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'
import type { components } from '@/generated/openapi/komga'

export const useCreateUser = defineMutation(() => {
  const queryCache = useQueryCache()
  return useMutation({
    mutation: (user: components['schemas']['UserCreationDto']) =>
      komgaClient.POST('/api/v2/users', {
        body: user,
      }),
    onSuccess: () => {
      void queryCache.invalidateQueries({ key: ['users'] })
    },
    onError: (error) => {
      console.log('create user error', error)
    },
  })
})
export const useUpdateUser = defineMutation(() => {
  const queryCache = useQueryCache()
  return useMutation({
    mutation: (user: components['schemas']['UserDto']) =>
      komgaClient.PATCH('/api/v2/users/{id}', {
        params: { path: { id: user.id } },
        body: user,
      }),
    onSuccess: () => {
      void queryCache.invalidateQueries({ key: ['users'] })
    },
    onError: (error) => {
      console.log('update user error', error)
    },
  })
})

export const useUpdateUserPassword = defineMutation(() => {
  return useMutation({
    mutation: ({ userId, newPassword }: { userId: string; newPassword: string }) =>
      komgaClient.PATCH('/api/v2/users/{id}/password', {
        params: { path: { id: userId } },
        body: {
          password: newPassword,
        },
      }),
    onError: (error) => {
      console.log('update user password error', error)
    },
  })
})

export const useDeleteUser = defineMutation(() => {
  const queryCache = useQueryCache()
  return useMutation({
    mutation: (userId: string) =>
      komgaClient.DELETE('/api/v2/users/{id}', {
        params: { path: { id: userId } },
      }),
    onSuccess: () => {
      void queryCache.invalidateQueries({ key: ['users'] })
    },
    onError: (error) => {
      console.log('delete user error', error)
    },
  })
})
