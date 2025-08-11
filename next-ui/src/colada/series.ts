import { defineQueryOptions } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'

export const QUERY_KEYS_SERIES = {
  root: ['series'] as const,
  byId: (seriesId: string) => [...QUERY_KEYS_SERIES.root, seriesId] as const,
}

export const seriesDetailQuery = defineQueryOptions(({ seriesId }: { seriesId: string }) => ({
  key: QUERY_KEYS_SERIES.byId(seriesId),
  query: () =>
    komgaClient
      .GET('/api/v1/series/{seriesId}', {
        params: {
          path: {
            seriesId: seriesId,
          },
        },
      })
      // unwrap the openapi-fetch structure on success
      .then((res) => res.data),
}))
