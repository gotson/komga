import {
  defineInfiniteQueryOptions,
  defineMutation,
  defineQueryOptions,
  useMutation,
} from '@pinia/colada'
import { PageRequest, type Sort, sortToString } from '@/types/PageRequest'
import { seriesMetadataToDto } from '@/functions/series'
import { entityChanged } from '@/colada/cache'
import { useAppStore } from '@/stores/app'
import {
  komgaDeleteSeriesFile,
  komgaGetSeries,
  komgaGetSeriesById,
  komgaMarkSeriesAsRead,
  komgaMarkSeriesAsUnread,
  komgaSeriesAnalyze,
  komgaSeriesRefreshMetadata,
  komgaUpdateSeriesMetadata,
  type SeriesMetadataDto,
  type SeriesSearch,
} from '@/generated/openapi'

export const QUERY_KEYS_SERIES = {
  root: ['series'] as const,
  bySearch: (request: object) => [...QUERY_KEYS_SERIES.root, JSON.stringify(request)] as const,
  byId: (seriesId: string) => [...QUERY_KEYS_SERIES.root, seriesId] as const,
}

export const seriesListQuery = defineQueryOptions(
  ({ search, pageRequest }: { search: SeriesSearch; pageRequest?: PageRequest }) => ({
    key: QUERY_KEYS_SERIES.bySearch({ search: search, pageRequest: pageRequest }),
    query: () =>
      komgaGetSeries({
        body: search,
        query: {
          ...pageRequest,
        },
      }),
    placeholderData: (previousData) => previousData,
  }),
)

export const seriesListQueryInfinite = defineInfiniteQueryOptions(
  ({ search, sort }: { search: SeriesSearch; sort?: Sort[] }) => ({
    key: QUERY_KEYS_SERIES.bySearch({ search: search, sort: sort, infinite: true }),
    initialPageParam: new PageRequest(0, 50, sort),
    query: ({ pageParam }) =>
      komgaGetSeries({
        body: search,
        query: {
          page: pageParam.page,
          size: pageParam.size,
          sort: sort?.map((it) => sortToString(it)),
        },
      }),
    getNextPageParam: (lastPage, _, lastPageParam) =>
      !lastPage?.last ? lastPageParam.next() : null,
  }),
)

export const seriesDetailQuery = defineQueryOptions(({ seriesId }: { seriesId: string }) => ({
  key: QUERY_KEYS_SERIES.byId(seriesId),
  query: () =>
    komgaGetSeriesById({
      path: {
        seriesId: seriesId,
      },
    }),
}))

export const useRefreshMetadataSeries = defineMutation(() =>
  useMutation({
    mutation: (seriesId: string) =>
      komgaSeriesRefreshMetadata({
        path: {
          seriesId: seriesId,
        },
      }),
  }),
)

export const useAnalyzeSeries = defineMutation(() =>
  useMutation({
    mutation: (seriesId: string) =>
      komgaSeriesAnalyze({
        path: {
          seriesId: seriesId,
        },
      }),
  }),
)

export const useDeleteSeries = defineMutation(() => {
  const appStore = useAppStore()
  return useMutation({
    mutation: (seriesId: string) =>
      komgaDeleteSeriesFile({
        path: {
          seriesId: seriesId,
        },
      }),
    onSuccess: (_data, seriesId) => {
      if (appStore.sseUnavailable) entityChanged(QUERY_KEYS_SERIES.root, seriesId)
    },
  })
})

export const useMarkSeriesRead = defineMutation(() => {
  const appStore = useAppStore()
  return useMutation({
    mutation: (seriesId: string) =>
      komgaMarkSeriesAsRead({
        path: {
          seriesId: seriesId,
        },
      }),
    onSuccess: (_data, seriesId) => {
      if (appStore.sseUnavailable) entityChanged(QUERY_KEYS_SERIES.root, seriesId)
    },
  })
})

export const useMarkSeriesUnread = defineMutation(() => {
  const appStore = useAppStore()
  return useMutation({
    mutation: (seriesId: string) =>
      komgaMarkSeriesAsUnread({
        path: {
          seriesId: seriesId,
        },
      }),
    onSuccess: (_data, seriesId) => {
      if (appStore.sseUnavailable) entityChanged(QUERY_KEYS_SERIES.root, seriesId)
    },
  })
})

export const useUpdateSeriesMetadata = defineMutation(() => {
  const appStore = useAppStore()
  return useMutation({
    mutation: ({ seriesId, metadata }: { seriesId: string; metadata: SeriesMetadataDto }) =>
      komgaUpdateSeriesMetadata({
        path: {
          seriesId: seriesId,
        },
        body: seriesMetadataToDto(metadata),
      }),
    onSuccess: (_data, { seriesId }) => {
      if (appStore.sseUnavailable) entityChanged(QUERY_KEYS_SERIES.root, seriesId)
    },
  })
})
