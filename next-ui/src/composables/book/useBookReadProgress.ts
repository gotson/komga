import type { BookDto } from '@/generated/openapi'

export function useBookReadProgress(book: MaybeRefOrGetter<BookDto>) {
  const isRead = computed(() => toValue(book).readProgress?.completed || false)
  const inProgress = computed(() => (toValue(book).readProgress && !isRead.value) || false)

  /**
   * Progress percentage if the book is in progress, else undefined
   */
  const progressPercent = computed(() => {
    const b = toValue(book)
    if (b.readProgress?.completed === false) {
      return (b.readProgress?.page / b.media.pagesCount) * 100
    }
  })

  /**
   * Pages left if the book is in progress, else undefined
   */
  const pagesLeft = computed(() => {
    const b = toValue(book)
    if (b.readProgress?.completed === false) {
      return b.media.pagesCount - b.readProgress?.page
    }
  })

  return {
    isRead,
    inProgress,
    progressPercent,
    pagesLeft,
  }
}
