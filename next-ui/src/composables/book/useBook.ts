import { useCurrentUser } from '@/colada/users'
import { useGetLibrariesById } from '@/composables/libraries'
import type { BookDto } from '@/generated/openapi'
import { MediaStatus } from '@/types/MediaStatus'

export function useBook(book: MaybeRefOrGetter<BookDto>) {
  const { hasRole } = useCurrentUser()
  const { libraries } = useGetLibrariesById(() => toValue(book).libraryId)

  const isDeleted = computed(() => toValue(book).deleted)
  const isUnavailable = computed(() => isDeleted.value || libraries.value?.[0]?.unavailable)

  const isNotReady = computed(() => toValue(book).media.status !== MediaStatus.Ready)

  const canRead = computed(
    () =>
      toValue(book).media.status === MediaStatus.Ready &&
      hasRole('PAGE_STREAMING') &&
      !isUnavailable.value,
  )

  const isEpubReader = computed(() => {
    const b = toValue(book)
    return b.media.mediaProfile.toLowerCase() === 'epub' && !b.media.epubDivinaCompatible
  })

  const format = computed(() => {
    const b = toValue(book)
    if (b.media.mediaType.includes('x-rar-compressed')) return 'CBR'
    if (b.media.mediaType === 'application/zip') return 'CBZ'
    if (b.media.mediaType === 'application/pdf') return 'PDF'
    if (b.media.mediaType === 'application/epub+zip') return 'EPUB'
    return b.media.mediaType
  })

  return {
    isUnavailable,
    canRead,
    isEpubReader,
    isNotReady,
    isDeleted,
    format,
  }
}
