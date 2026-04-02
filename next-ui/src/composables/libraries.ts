import { useLibraries } from '@/colada/libraries'
import type { LibraryId } from '@/types/libraries'
import type { components } from '@/generated/openapi/komga'

/**
 * A composable that returns libraries filtered by a LibraryId.
 * @param libraryId the library ID or group to get
 */
export function useGetLibrariesById(libraryId: MaybeRefOrGetter<LibraryId>) {
  const { data: all, pinned, unpinned, status } = useLibraries()

  const libs = computed(() => {
    if (status.value !== 'success') return undefined

    let libs: components['schemas']['LibraryDto'][] = []
    switch (toValue(libraryId)) {
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
        const lib = all.value?.find((it) => it.id === toValue(libraryId))
        if (lib) libs = [lib]
        break
    }
    return libs
  })

  const libIds = computed(() => libs.value?.map((it) => it.id))

  const isPinned = computed(() => toValue(libraryId) === 'pinned')
  const isUnpinned = computed(() => toValue(libraryId) === 'unpinned')
  const isAll = computed(() => toValue(libraryId) === 'all')
  const isSingle = computed(() => !isPinned.value && !isUnpinned.value && !isAll.value)

  return {
    libraries: libs,
    libraryIds: libIds,
    isPinned,
    isUnpinned,
    isAll,
    isSingle,
  }
}
