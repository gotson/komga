import { defineQueryOptions } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'

export const historyQuery = defineQueryOptions(
  ({ page, size, sort }: { page?: number; size?: number; sort?: string[] }) => ({
    key: ['history', { page: page, size: size, sort: sort }],
    query: () =>
      komgaClient
        .GET('/api/v1/history', {
          params: {
            query: { page: page, size: size, sort: sort },
          },
        })
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
    placeholderData: (previousData: any) => previousData, // eslint-disable-line @typescript-eslint/no-explicit-any
  }),
)
