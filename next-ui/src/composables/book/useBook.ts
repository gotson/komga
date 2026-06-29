import { useCurrentUser } from '@/colada/users'
import { useGetLibrariesById } from '@/composables/libraries'
import { MediaStatus } from '@/types/MediaStatus'
import { UserRoles } from '@/types/UserRoles'
import { getEnumValueFromString } from '@/functions/enum'
import type { BookDto } from '@/generated/openapi'

export function useBook(book: MaybeRefOrGetter<BookDto>) {
  const { hasRole } = useCurrentUser()
  const { libraries } = useGetLibrariesById(() => toValue(book).libraryId)

  const isDeleted = computed(() => toValue(book).deleted)
  const isUnavailable = computed(() => isDeleted.value || libraries.value?.[0]?.unavailable)

  const isNotReady = computed(() => toValue(book).media.status !== MediaStatus.READY.valueOf())

  const mediaStatus = computed(() =>
    getEnumValueFromString(MediaStatus, toValue(book).media.status),
  )

  const canRead = computed(
    () =>
      toValue(book).media.status === MediaStatus.READY.valueOf() &&
      hasRole(UserRoles.PAGE_STREAMING) &&
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
    mediaStatus,
    isUnavailable,
    canRead,
    isEpubReader,
    isNotReady,
    isDeleted,
    format,
  }
}
