import { defineMutation, defineQuery, useMutation, useQuery } from '@pinia/colada'
import { useClientSettingsUser } from '@/colada/client-settings'
import { combinePromises } from '@/colada/utils'
import { entitiesChanged } from '@/colada/cache'
import { useAppStore } from '@/stores/app'
import {
  komgaAddLibrary,
  komgaDeleteLibraryById,
  komgaGetLibraries,
  komgaLibraryAnalyze,
  komgaLibraryEmptyTrash,
  komgaLibraryRefreshMetadata,
  komgaLibraryScan,
  komgaUpdateLibraryById,
  type LibraryCreationDto,
  type LibraryDto,
} from '@/generated/openapi'

export const QUERY_KEYS_LIBRARIES = {
  root: ['libraries'] as const,
}

export const useLibraries = defineQuery(() => {
  const {
    data,
    refresh: refreshLibraries,
    refetch: refetchLibraries,
    ...rest
  } = useQuery({
    key: () => QUERY_KEYS_LIBRARIES.root,
    query: () => komgaGetLibraries(),
    // 1 hour
    staleTime: 60 * 60 * 1000,
    gcTime: false,
  })

  const {
    userLibraries,
    refresh: refreshSettings,
    refetch: refetchSettings,
  } = useClientSettingsUser()

  const refresh = combinePromises(refreshLibraries, [refreshSettings])
  const refetch = combinePromises(refetchLibraries, [refetchSettings])

  const ordered = computed(() =>
    data?.value?.sort(
      (a, b) =>
        (userLibraries.value?.[a.id]?.order || 0) - (userLibraries.value?.[b.id]?.order || 0),
    ),
  )

  const unpinned = computed(
    () => ordered.value?.filter((it) => userLibraries.value?.[it.id]?.unpinned) || [],
  )
  const pinned = computed(
    () => ordered.value?.filter((it) => !userLibraries.value?.[it.id]?.unpinned) || [],
  )
  const anyPinned = computed(() => pinned.value.length > 0)
  const anyUnpinned = computed(() => unpinned.value.length > 0)

  return {
    data,
    ordered,
    unpinned,
    pinned,
    anyPinned,
    anyUnpinned,
    refresh,
    refetch,
    ...rest,
  }
})

export const useCreateLibrary = defineMutation(() => {
  const appStore = useAppStore()
  return useMutation({
    mutation: (library: LibraryCreationDto) =>
      komgaAddLibrary({
        body: library,
      }),
    onSuccess: () => {
      if (appStore.sseUnavailable) entitiesChanged(QUERY_KEYS_LIBRARIES.root)
    },
  })
})

export const useUpdateLibrary = defineMutation(() => {
  const appStore = useAppStore()
  return useMutation({
    mutation: (library: LibraryDto) =>
      komgaUpdateLibraryById({
        body: library,
        path: { libraryId: library.id },
      }),
    onSuccess: () => {
      if (appStore.sseUnavailable) entitiesChanged(QUERY_KEYS_LIBRARIES.root)
    },
  })
})

export const useDeleteLibrary = defineMutation(() => {
  const appStore = useAppStore()
  return useMutation({
    mutation: (libraryId: string) =>
      komgaDeleteLibraryById({
        path: {
          libraryId: libraryId,
        },
      }),
    onSuccess: () => {
      if (appStore.sseUnavailable) entitiesChanged(QUERY_KEYS_LIBRARIES.root)
    },
  })
})

export const useRefreshMetadataLibrary = defineMutation(() =>
  useMutation({
    mutation: (libraryId: string) =>
      komgaLibraryRefreshMetadata({
        path: {
          libraryId: libraryId,
        },
      }),
  }),
)

export const useEmptyTrashLibrary = defineMutation(() =>
  useMutation({
    mutation: (libraryId: string) =>
      komgaLibraryEmptyTrash({
        path: {
          libraryId: libraryId,
        },
      }),
  }),
)

export const useScanLibrary = defineMutation(() =>
  useMutation({
    mutation: ({ libraryId, deep = false }: { libraryId: string; deep?: boolean }) =>
      komgaLibraryScan({
        path: {
          libraryId: libraryId,
        },
        query: {
          deep: deep,
        },
      }),
  }),
)

export const useAnalyzeLibrary = defineMutation(() =>
  useMutation({
    mutation: (libraryId: string) =>
      komgaLibraryAnalyze({
        path: {
          libraryId: libraryId,
        },
      }),
  }),
)
