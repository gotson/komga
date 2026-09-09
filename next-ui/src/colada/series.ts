import {
  defineInfiniteQueryOptions,
  defineMutation,
  defineQueryOptions,
  useMutation,
} from '@pinia/colada'
import { PageRequest, type Sort, sortToString } from '@/types/PageRequest'
import { seriesMetadataToUpdateDto } from '@/functions/series'
import { entitiesChanged, entityChanged } from '@/colada/cache'
import { useAppStore } from '@/stores/app'
import {
  komgaAddUserUploadedSeriesThumbnail,
  komgaDeleteSeriesFile,
  komgaDeleteUserUploadedSeriesThumbnail,
  komgaGetSeries,
  komgaGetSeriesById,
  komgaGetSeriesThumbnails,
  komgaGetSeriesUpdated,
  komgaMarkSeriesAsRead,
  komgaMarkSeriesAsUnread,
  komgaMarkSeriesThumbnailSelected,
  komgaSeriesAnalyze,
  komgaSeriesRefreshMetadata,
  komgaUpdateSeriesMetadata,
  type SeriesMetadataDto,
  type SeriesSearch,
} from '@/generated/openapi'
import { useImageCacheStore } from '@/stores/image-cache'

export const QUERY_KEYS_SERIES = {
  root: ['series'] as const,
  bySearch: (request: object) => [...QUERY_KEYS_SERIES.root, JSON.stringify(request)] as const,
  byId: (seriesId: string) => [...QUERY_KEYS_SERIES.root, seriesId] as const,
  updated: (request: object) =>
    [...QUERY_KEYS_SERIES.root, 'updated', JSON.stringify(request)] as const,
  posters: (id: string) => [...QUERY_KEYS_SERIES.byId(id), 'posters'] as const,
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

export const seriesUpdatedQueryInfinite = defineInfiniteQueryOptions(
  ({ libraryIds }: { libraryIds?: string[] }) => ({
    key: QUERY_KEYS_SERIES.updated({ libraryIds: libraryIds, infinite: true }),
    initialPageParam: new PageRequest(0, 50),
    query: ({ pageParam }) =>
      komgaGetSeriesUpdated({
        query: {
          library_id: libraryIds,
          oneshot: false,
          page: pageParam.page,
          size: pageParam.size,
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
    mutation: ({
      seriesId,
      metadata,
    }: {
      seriesId: string
      metadata: Partial<SeriesMetadataDto>
    }) =>
      komgaUpdateSeriesMetadata({
        path: {
          seriesId: seriesId,
        },
        body: seriesMetadataToUpdateDto(metadata),
      }),
    onSuccess: (_data, { seriesId }) => {
      if (appStore.sseUnavailable) entityChanged(QUERY_KEYS_SERIES.root, seriesId)
    },
  })
})

export const seriesPostersQuery = defineQueryOptions(({ seriesId }: { seriesId: string }) => ({
  key: QUERY_KEYS_SERIES.posters(seriesId),
  query: () =>
    komgaGetSeriesThumbnails({
      path: {
        seriesId: seriesId,
      },
    }),
}))

export const useAddSeriesPoster = defineMutation(() => {
  const appStore = useAppStore()
  const cacheStore = useImageCacheStore()
  return useMutation({
    mutation: ({ seriesId, file, selected }: { seriesId: string; file: File; selected: boolean }) =>
      komgaAddUserUploadedSeriesThumbnail({
        query: {
          selected: selected,
        },
        body: {
          file: file,
        },
        path: {
          seriesId: seriesId,
        },
      }),
    onSuccess: (_data, { seriesId }) => {
      if (appStore.sseUnavailable) {
        entitiesChanged(QUERY_KEYS_SERIES.posters(seriesId))
        cacheStore.bustCache(seriesId)
      }
    },
  })
})

export const useDeleteSeriesPoster = defineMutation(() => {
  const appStore = useAppStore()
  const cacheStore = useImageCacheStore()
  return useMutation({
    mutation: ({ seriesId, thumbnailId }: { seriesId: string; thumbnailId: string }) =>
      komgaDeleteUserUploadedSeriesThumbnail({
        path: {
          seriesId: seriesId,
          thumbnailId: thumbnailId,
        },
      }),
    onSuccess: (_data, { seriesId }) => {
      if (appStore.sseUnavailable) {
        entitiesChanged(QUERY_KEYS_SERIES.posters(seriesId))
        cacheStore.bustCache(seriesId)
      }
    },
  })
})

export const useMarkSeriesPosterSelected = defineMutation(() => {
  const appStore = useAppStore()
  const cacheStore = useImageCacheStore()
  return useMutation({
    mutation: ({ seriesId, thumbnailId }: { seriesId: string; thumbnailId: string }) =>
      komgaMarkSeriesThumbnailSelected({
        path: {
          seriesId: seriesId,
          thumbnailId: thumbnailId,
        },
      }),
    onSuccess: (_data, { seriesId }) => {
      if (appStore.sseUnavailable) {
        entitiesChanged(QUERY_KEYS_SERIES.posters(seriesId))
        cacheStore.bustCache(seriesId)
      }
    },
  })
})
