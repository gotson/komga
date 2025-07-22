import { defineMutation, useMutation, useQueryCache } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'

export const useDeleteSyncPoints = defineMutation(() => {
  return useMutation({
    mutation: (keyIds: string[]) =>
      komgaClient.DELETE('/api/v1/syncpoints/me', {
        params: { query: { key_id: keyIds } },
      }),
  })
})
