import {
  defineInfiniteQueryOptions,
  defineMutation,
  defineQueryOptions,
  useMutation,
} from '@pinia/colada'
import { PageRequest } from '@/types/PageRequest'
import { entitiesChanged, entityChanged } from '@/colada/cache'
import { useAppStore } from '@/stores/app'
import {
  komgaCreateReadList,
  komgaDeleteReadListById,
  komgaGetReadListById,
  komgaGetReadLists,
  komgaUpdateReadListById,
  type ReadListCreationDto,
  type ReadListUpdateDto,
} from '@/generated/openapi'

export const QUERY_KEYS_READLIST = {
  root: ['readlists'] as const,
  bySearch: (request: object) => [...QUERY_KEYS_READLIST.root, JSON.stringify(request)] as const,
  byId: (id: string) => [...QUERY_KEYS_READLIST.root, id] as const,
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
          search: search,
          library_id: libraryIds,
          ...pageRequest,
        },
      }),
    placeholderData: (previousData) => previousData,
  }),
)

export const readListsListQueryInfinite = defineInfiniteQueryOptions(
  ({ libraryIds }: { libraryIds?: string[] }) => ({
    key: QUERY_KEYS_READLIST.bySearch({
      libraryIds: libraryIds,
      infinite: true,
    }),
    initialPageParam: new PageRequest(0, 50),
    query: ({ pageParam }) =>
      komgaGetReadLists({
        query: {
          page: pageParam.page,
          size: pageParam.size,
          library_id: libraryIds,
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
