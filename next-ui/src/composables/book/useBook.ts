import { useCurrentUser } from '@/colada/users'
import { useGetLibrariesByViewId } from '@/composables/libraries'
import type { BookDto } from '@/generated/openapi'
import { MediaStatus } from '@/types/MediaStatus'
import { defineMessage } from 'vue-intl'

export function useBook(book: MaybeRefOrGetter<BookDto>) {
  const { hasRole } = useCurrentUser()
  const { libraries } = useGetLibrariesByViewId(() => toValue(book).libraryId)

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
    if (b.media.mediaType.includes('x-rar-compressed'))
      return defineMessage({
        description: 'Book file format: CBR',
        defaultMessage: 'CBR',
        id: 'lkoTf7',
      })
    if (b.media.mediaType === 'application/zip')
      return defineMessage({
        description: 'Book file format: CBZ',
        defaultMessage: 'CBZ',
        id: '9aVhJC',
      })
    if (b.media.mediaType === 'application/pdf')
      return defineMessage({
        description: 'Book file format: PDF',
        defaultMessage: 'PDF',
        id: 'EnHAAa',
      })
    if (b.media.mediaType === 'application/epub+zip')
      return defineMessage({
        description: 'Book file format: EPUB',
        defaultMessage: 'EPUB',
        id: 'iQKrku',
      })
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
