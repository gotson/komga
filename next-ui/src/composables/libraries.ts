import { useLibraries } from '@/colada/libraries'
import type { LibraryViewId } from '@/types/libraries'
import type { LibraryDto } from '@/generated/openapi'

/**
 * A composable that returns libraries filtered by a LibraryViewId.
 * @param libraryViewId the library ID or group to get
 */
export function useGetLibrariesByViewId(libraryViewId: MaybeRefOrGetter<LibraryViewId>) {
  const { data: all, pinned, unpinned, status } = useLibraries()

  const libs = computed(() => {
    if (status.value !== 'success') return undefined

    let libs: LibraryDto[] = []
    switch (toValue(libraryViewId)) {
      case 'all':
        libs = all.value || []
        break
      case 'pinned':
        libs = pinned.value
        break
      case 'unpinned':
        libs = unpinned.value
        break
      default:
        const lib = all.value?.find((it) => it.id === toValue(libraryViewId))
        if (lib) libs = [lib]
        break
    }
    return libs
  })

  const libIds = computed(() => libs.value?.map((it) => it.id))

  const isPinned = computed(() => toValue(libraryViewId) === 'pinned')
  const isUnpinned = computed(() => toValue(libraryViewId) === 'unpinned')
  const isAll = computed(() => toValue(libraryViewId) === 'all')
  const isSingle = computed(() => !isPinned.value && !isUnpinned.value && !isAll.value)

  const library = computed(() => (isSingle.value ? libs.value?.[0] : undefined))

  return {
    libraries: libs,
    library,
    libraryIds: libIds,
    isPinned,
    isUnpinned,
    isAll,
    isSingle,
  }
}
