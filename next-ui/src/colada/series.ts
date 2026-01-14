import { defineMutation, defineQueryOptions, useMutation } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'
import type { components } from '@/generated/openapi/komga'
import type { PageRequest } from '@/types/PageRequest'
import { seriesMetadataToDto } from '@/functions/series'

export const QUERY_KEYS_SERIES = {
  root: ['series'] as const,
  bySearch: (request: object) => [...QUERY_KEYS_SERIES.root, JSON.stringify(request)] as const,
  byId: (seriesId: string) => [...QUERY_KEYS_SERIES.root, seriesId] as const,
}

export const seriesListQuery = defineQueryOptions(
  ({
    search,
    pause = false,
    pageRequest,
  }: {
    search: components['schemas']['SeriesSearch']
    pause?: boolean
    pageRequest?: PageRequest
  }) => ({
    key: QUERY_KEYS_SERIES.bySearch({ search: search, pageRequest: pageRequest }),
    query: () =>
      komgaClient
        .POST('/api/v1/series/list', {
          body: search,
          params: {
            query: {
              ...pageRequest,
            },
          },
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

export const useRefreshMetadataSeries = defineMutation(() =>
  useMutation({
    mutation: (seriesId: string) =>
      komgaClient.POST('/api/v1/series/{seriesId}/metadata/refresh', {
        params: {
          path: {
            seriesId: seriesId,
          },
        },
      }),
  }),
)

export const useAnalyzeSeries = defineMutation(() =>
  useMutation({
    mutation: (seriesId: string) =>
      komgaClient.POST('/api/v1/series/{seriesId}/analyze', {
        params: {
          path: {
            seriesId: seriesId,
          },
        },
      }),
  }),
)

export const useDeleteSeries = defineMutation(() =>
  useMutation({
    mutation: (seriesId: string) =>
      komgaClient.DELETE('/api/v1/series/{seriesId}/file', {
        params: {
          path: {
            seriesId: seriesId,
          },
        },
      }),
  }),
)

export const useMarkSeriesRead = defineMutation(() =>
  useMutation({
    mutation: (seriesId: string) =>
      komgaClient.POST('/api/v1/series/{seriesId}/read-progress', {
        params: {
          path: {
            seriesId: seriesId,
          },
        },
      }),
  }),
)

export const useMarkSeriesUnread = defineMutation(() =>
  useMutation({
    mutation: (seriesId: string) =>
      komgaClient.DELETE('/api/v1/series/{seriesId}/read-progress', {
        params: {
          path: {
            seriesId: seriesId,
          },
        },
      }),
  }),
)

export const useUpdateSeriesMetadata = defineMutation(() => {
  // const queryCache = useQueryCache()
  return useMutation({
    mutation: ({
      seriesId,
      metadata,
    }: {
      seriesId: string
      metadata: components['schemas']['SeriesMetadataDto']
    }) =>
      komgaClient.PATCH('/api/v1/series/{seriesId}/metadata', {
        params: {
          path: {
            seriesId: seriesId,
          },
        },
        body: seriesMetadataToDto(metadata),
      }),
    onSuccess: () => {
      //TODO: check how to invalidate cache
      // void queryCache.invalidateQueries({ key: QUERY_KEYS_LIBRARIES.root })
    },
  })
})
