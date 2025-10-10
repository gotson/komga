import { defineMutation, useMutation } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'
import type { components } from '@/generated/openapi/komga'

export const QUERY_KEYS_READLIST = {
  root: ['readlists'] as const,
}

export const useCreateReadList = defineMutation(() => {
  return useMutation({
    mutation: (readList: components['schemas']['ReadListCreationDto']) =>
      komgaClient.POST('/api/v1/readlists', {
        body: readList,
      }),
  })
})
