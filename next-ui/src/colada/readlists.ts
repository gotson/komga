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
  komgaAddUserUploadedReadListThumbnail,
  komgaCreateReadList,
  komgaDeleteReadListById,
  komgaDeleteUserUploadedReadListThumbnail,
  komgaGetReadListById,
  komgaGetReadLists,
  komgaGetReadListThumbnails,
  komgaMarkReadListThumbnailSelected,
  komgaUpdateReadListById,
  type ReadListCreationDto,
  type ReadListUpdateDto,
} from '@/generated/openapi'
import { useImageCacheStore } from '@/stores/image-cache'

export const QUERY_KEYS_READLIST = {
  root: ['readlists'] as const,
  bySearch: (request: object) => [...QUERY_KEYS_READLIST.root, JSON.stringify(request)] as const,
  byId: (id: string) => [...QUERY_KEYS_READLIST.root, id] as const,
  posters: (id: string) => [...QUERY_KEYS_READLIST.byId(id), 'posters'] as const,
}

export const readListsListQuery = defineQueryOptions(
  ({
    search,
    libraryIds,
    pageRequest,
  }: {
    search?: string
    libraryIds?: string[]
    pageRequest?: PageRequest
  }) => ({
    key: QUERY_KEYS_READLIST.bySearch({
      search: search,
      libraryIds: libraryIds,
      pageRequest: pageRequest,
    }),
    query: () =>
      komgaGetReadLists({
        query: {
          search: search || undefined,
          library_id: libraryIds,
          ...pageRequest,
        },
      }),
    placeholderData: (previousData) => previousData,
  }),
)

export const readListsListQueryInfinite = defineInfiniteQueryOptions(
  ({ search, libraryIds, sort }: { search?: string; libraryIds?: string[]; sort?: Sort[] }) => ({
    key: QUERY_KEYS_READLIST.bySearch({
      search: search,
      libraryIds: libraryIds,
      sort: sort,
      infinite: true,
    }),
    initialPageParam: new PageRequest(0, 50),
    query: ({ pageParam }) =>
      komgaGetReadLists({
        query: {
          page: pageParam.page,
          size: pageParam.size,
          search: search || undefined,
          library_id: libraryIds,
          sort: sort?.map((it) => sortToString(it)),
        },
      }),
    getNextPageParam: (lastPage, _, lastPageParam) =>
      !lastPage?.last ? lastPageParam.next() : null,
  }),
)

export const readListDetailQuery = defineQueryOptions(({ readListId }: { readListId: string }) => ({
  key: QUERY_KEYS_READLIST.byId(readListId),
  query: () =>
    komgaGetReadListById({
      path: {
        id: readListId,
      },
    }),
}))

export const useCreateReadList = defineMutation(() => {
  const appStore = useAppStore()
  return useMutation({
    mutation: (readList: ReadListCreationDto) =>
      komgaCreateReadList({
        body: readList,
      }),
    onSuccess: () => {
      if (appStore.sseUnavailable) entitiesChanged(QUERY_KEYS_READLIST.root)
    },
  })
})

export const useUpdateReadList = defineMutation(() => {
  const appStore = useAppStore()
  return useMutation({
    mutation: ({ readListId, data }: { readListId: string; data: ReadListUpdateDto }) =>
      komgaUpdateReadListById({
        path: {
          id: readListId,
        },
        body: data,
      }),
    onSuccess: (_data, { readListId }) => {
      if (appStore.sseUnavailable) entityChanged(QUERY_KEYS_READLIST.root, readListId)
    },
  })
})

export const useDeleteReadList = defineMutation(() => {
  const appStore = useAppStore()
  return useMutation({
    mutation: (readListId: string) =>
      komgaDeleteReadListById({
        path: {
          id: readListId,
        },
      }),
    onSuccess: (_data, readListId) => {
      if (appStore.sseUnavailable) entityChanged(QUERY_KEYS_READLIST.root, readListId)
    },
  })
})

export const readListPostersQuery = defineQueryOptions(
  ({ readListId }: { readListId: string }) => ({
    key: QUERY_KEYS_READLIST.posters(readListId),
    query: () =>
      komgaGetReadListThumbnails({
        path: {
          id: readListId,
        },
      }),
  }),
)

export const useAddReadListPoster = defineMutation(() => {
  const appStore = useAppStore()
  const cacheStore = useImageCacheStore()
  return useMutation({
    mutation: ({
      readListId,
      file,
      selected,
    }: {
      readListId: string
      file: File
      selected: boolean
    }) =>
      komgaAddUserUploadedReadListThumbnail({
        query: {
          selected: selected,
        },
        body: {
          file: file,
        },
        path: {
          id: readListId,
        },
      }),
    onSuccess: (_data, { readListId }) => {
      if (appStore.sseUnavailable) {
        entitiesChanged(QUERY_KEYS_READLIST.posters(readListId))
        cacheStore.bustCache(readListId)
      }
    },
  })
})

export const useDeleteReadListPoster = defineMutation(() => {
  const appStore = useAppStore()
  const cacheStore = useImageCacheStore()
  return useMutation({
    mutation: ({ readListId, thumbnailId }: { readListId: string; thumbnailId: string }) =>
      komgaDeleteUserUploadedReadListThumbnail({
        path: {
          id: readListId,
          thumbnailId: thumbnailId,
        },
      }),
    onSuccess: (_data, { readListId }) => {
      if (appStore.sseUnavailable) {
        entitiesChanged(QUERY_KEYS_READLIST.posters(readListId))
        cacheStore.bustCache(readListId)
      }
    },
  })
})

export const useMarkReadListPosterSelected = defineMutation(() => {
  const appStore = useAppStore()
  const cacheStore = useImageCacheStore()
  return useMutation({
    mutation: ({ readListId, thumbnailId }: { readListId: string; thumbnailId: string }) =>
      komgaMarkReadListThumbnailSelected({
        path: {
          id: readListId,
          thumbnailId: thumbnailId,
        },
      }),
    onSuccess: (_data, { readListId }) => {
      if (appStore.sseUnavailable) {
        entitiesChanged(QUERY_KEYS_READLIST.posters(readListId))
        cacheStore.bustCache(readListId)
      }
    },
  })
})
