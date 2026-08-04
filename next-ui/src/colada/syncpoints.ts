import { defineMutation, useMutation } from '@pinia/colada'
import { komgaDeleteSyncPointsForCurrentUser } from '@/generated/openapi'

export const useDeleteSyncPoints = defineMutation(() => {
  return useMutation({
    mutation: (keyIds: string[]) =>
      komgaDeleteSyncPointsForCurrentUser({
        query: { key_id: keyIds },
      }),
  })
})
