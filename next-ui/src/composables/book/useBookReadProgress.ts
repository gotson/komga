import type { components } from '@/generated/openapi/komga'

export function useBookReadProgress(book: MaybeRefOrGetter<components['schemas']['BookDto']>) {
  return computed(() => {
    const b = toValue(book)
    if (b.readProgress?.completed) return 100
    if (b.readProgress?.completed === false) {
      return (b.readProgress?.page / b.media.pagesCount) * 100
    }
    return 0
  })
}
