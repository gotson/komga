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

  return {
    libraries: libs,
  }
}
