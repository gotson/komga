import { defineQueryOptions } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'
import type { components } from '@/generated/openapi/komga'

export const QUERY_KEYS_SERIES = {
  root: ['series'] as const,
  bySearch: (search: components['schemas']['SeriesSearch']) =>
    [...QUERY_KEYS_SERIES.root, JSON.stringify(search)] as const,
  byId: (seriesId: string) => [...QUERY_KEYS_SERIES.root, seriesId] as const,
}

export const seriesListQuery = defineQueryOptions(
  ({
    search,
    pause = false,
  }: {
    search: components['schemas']['SeriesSearch']
    pause: boolean
  }) => ({
    key: QUERY_KEYS_SERIES.bySearch(search),
    query: () =>
      komgaClient
        .POST('/api/v1/series/list', {
          body: search,
        })
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
    enabled: !pause,
    placeholderData: (previousData: any) => previousData, // eslint-disable-line @typescript-eslint/no-explicit-any
  }),
)

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
