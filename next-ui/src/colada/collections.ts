import {
  defineInfiniteQueryOptions,
  defineMutation,
  defineQueryOptions,
  useMutation,
} from '@pinia/colada'
import { PageRequest, type Sort, sortToString } from '@/types/PageRequest'
import { entitiesChanged, entityChanged } from '@/colada/cache'
import { useAppStore } from '@/stores/app'
import {
  komgaDeleteCollectionById,
  komgaGetCollectionById,
  komgaGetCollections,
  komgaUpdateCollectionById,
  type CollectionUpdateDto,
  type CollectionCreationDto,
  komgaCreateCollection,
  komgaGetCollectionThumbnails,
  komgaAddUserUploadedCollectionThumbnail,
  komgaDeleteUserUploadedCollectionThumbnail,
  komgaMarkCollectionThumbnailSelected,
} from '@/generated/openapi'
import { useImageCacheStore } from '@/stores/image-cache'

export const QUERY_KEYS_COLLECTIONS = {
  root: ['collections'] as const,
  bySearch: (request: object) => [...QUERY_KEYS_COLLECTIONS.root, JSON.stringify(request)] as const,
  byId: (id: string) => [...QUERY_KEYS_COLLECTIONS.root, id] as const,
  posters: (id: string) => [...QUERY_KEYS_COLLECTIONS.byId(id), 'posters'] as const,
}

export const collectionsListQuery = defineQueryOptions(
  ({
    search,
    libraryIds,
    pageRequest,
  }: {
    search?: string
    libraryIds?: string[]
    pageRequest?: PageRequest
  }) => ({
    key: QUERY_KEYS_COLLECTIONS.bySearch({ search: search, libraryIds, pageRequest: pageRequest }),
    query: () =>
      komgaGetCollections({
        query: {
          search: search || undefined,
          library_id: libraryIds,
          ...pageRequest,
        },
      }),
    placeholderData: (previousData) => previousData,
  }),
)

export const collectionsListQueryInfinite = defineInfiniteQueryOptions(
  ({ search, libraryIds, sort }: { search?: string; libraryIds?: string[]; sort?: Sort[] }) => ({
    key: QUERY_KEYS_COLLECTIONS.bySearch({
      search: search,
      libraryIds,
      sort: sort,
      infinite: true,
    }),
    initialPageParam: new PageRequest(0, 50),
    query: ({ pageParam }) =>
      komgaGetCollections({
        query: {
          library_id: libraryIds,
          page: pageParam.page,
          size: pageParam.size,
          search: search || undefined,
          sort: sort?.map((it) => sortToString(it)),
        },
      }),
    getNextPageParam: (lastPage, _, lastPageParam) =>
      !lastPage?.last ? lastPageParam.next() : null,
  }),
)

export const collectionDetailQuery = defineQueryOptions(
  ({ collectionId }: { collectionId: string }) => ({
    key: QUERY_KEYS_COLLECTIONS.byId(collectionId),
    query: () =>
      komgaGetCollectionById({
        path: {
          id: collectionId,
        },
      }),
  }),
)

export const useCreateCollection = defineMutation(() => {
  const appStore = useAppStore()
  return useMutation({
    mutation: (collection: CollectionCreationDto) =>
      komgaCreateCollection({
        body: collection,
      }),
    onSuccess: () => {
      if (appStore.sseUnavailable) entitiesChanged(QUERY_KEYS_COLLECTIONS.root)
    },
  })
})

export const useUpdateCollection = defineMutation(() => {
  const appStore = useAppStore()
  return useMutation({
    mutation: ({ collectionId, data }: { collectionId: string; data: CollectionUpdateDto }) =>
      komgaUpdateCollectionById({
        path: {
          id: collectionId,
        },
        body: data,
      }),
    onSuccess: (_data, { collectionId }) => {
      if (appStore.sseUnavailable) entityChanged(QUERY_KEYS_COLLECTIONS.root, collectionId)
    },
  })
})

export const useDeleteCollection = defineMutation(() => {
  const appStore = useAppStore()
  return useMutation({
    mutation: (collectionId: string) =>
      komgaDeleteCollectionById({
        path: {
          id: collectionId,
        },
      }),
    onSuccess: (_data, collectionId) => {
      if (appStore.sseUnavailable) entityChanged(QUERY_KEYS_COLLECTIONS.root, collectionId)
    },
  })
})

export const collectionPostersQuery = defineQueryOptions(
  ({ collectionId }: { collectionId: string }) => ({
    key: QUERY_KEYS_COLLECTIONS.posters(collectionId),
    query: () =>
      komgaGetCollectionThumbnails({
        path: {
          id: collectionId,
        },
      }),
  }),
)

export const useAddCollectionPoster = defineMutation(() => {
  const appStore = useAppStore()
  const cacheStore = useImageCacheStore()
  return useMutation({
    mutation: ({
      collectionId,
      file,
      selected,
    }: {
      collectionId: string
      file: File
      selected: boolean
    }) =>
      komgaAddUserUploadedCollectionThumbnail({
        query: {
          selected: selected,
        },
        body: {
          file: file,
        },
        path: {
          id: collectionId,
        },
      }),
    onSuccess: (_data, { collectionId }) => {
      if (appStore.sseUnavailable) {
        entitiesChanged(QUERY_KEYS_COLLECTIONS.posters(collectionId))
        cacheStore.bustCache(collectionId)
      }
    },
  })
})

export const useDeleteCollectionPoster = defineMutation(() => {
  const appStore = useAppStore()
  const cacheStore = useImageCacheStore()
  return useMutation({
    mutation: ({ collectionId, thumbnailId }: { collectionId: string; thumbnailId: string }) =>
      komgaDeleteUserUploadedCollectionThumbnail({
        path: {
          id: collectionId,
          thumbnailId: thumbnailId,
        },
      }),
    onSuccess: (_data, { collectionId }) => {
      if (appStore.sseUnavailable) {
        entitiesChanged(QUERY_KEYS_COLLECTIONS.posters(collectionId))
        cacheStore.bustCache(collectionId)
      }
    },
  })
})

export const useMarkCollectionPosterSelected = defineMutation(() => {
  const appStore = useAppStore()
  const cacheStore = useImageCacheStore()
  return useMutation({
    mutation: ({ collectionId, thumbnailId }: { collectionId: string; thumbnailId: string }) =>
      komgaMarkCollectionThumbnailSelected({
        path: {
          id: collectionId,
          thumbnailId: thumbnailId,
        },
      }),
    onSuccess: (_data, { collectionId }) => {
      if (appStore.sseUnavailable) {
        entitiesChanged(QUERY_KEYS_COLLECTIONS.posters(collectionId))
        cacheStore.bustCache(collectionId)
      }
    },
  })
})
