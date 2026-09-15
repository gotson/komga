import { defineMutation, defineQueryOptions, useMutation, useQuery } from '@pinia/colada'
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

export const librariesQuery = defineQueryOptions({
  key: QUERY_KEYS_LIBRARIES.root,
  query: () => komgaGetLibraries(),
  // 1 hour
  staleTime: 60 * 60 * 1000,
  gcTime: false,
})

export const useLibraries = () => useQuery(librariesQuery)

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
