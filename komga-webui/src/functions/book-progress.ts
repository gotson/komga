import { ReadProgress } from '@/types/enum-books'

export function getReadProgress (book: BookDto): ReadProgress {
  if (book.readProgress?.completed) return ReadProgress.READ
  if (book.readProgress?.completed === false) return ReadProgress.IN_PROGRESS
  return ReadProgress.UNREAD
}

export function getReadProgressPercentage (book: BookDto): number {
  if (book.readProgress?.completed) return 100
  if (book.readProgress?.completed === false) {
    return book.readProgress?.page / book.media.pagesCount * 100
  }
  return 0
}
