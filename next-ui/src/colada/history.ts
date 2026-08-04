import { defineQueryOptions } from '@pinia/colada'
import { komgaGetHistoricalEvents } from '@/generated/openapi'

export const historyQuery = defineQueryOptions(
  ({ page, size, sort }: { page?: number; size?: number; sort?: string[] }) => ({
    key: ['history', { page: page, size: size, sort: sort }],
    query: () =>
      komgaGetHistoricalEvents({
        query: { page: page, size: size, sort: sort },
      }),
    placeholderData: (previousData) => previousData,
  }),
)
