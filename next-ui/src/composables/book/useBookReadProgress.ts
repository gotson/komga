import type { components } from '@/generated/openapi/komga'

export function useBookReadProgress(book: components['schemas']['BookDto']) {
  if (book.readProgress?.completed) return 100
  if (book.readProgress?.completed === false) {
    return (book.readProgress?.page / book.media.pagesCount) * 100
  }
  return 0
}
