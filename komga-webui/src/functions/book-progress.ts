import { ReadStatus } from '@/types/enum-books'

export function getReadProgress (book: BookDto): ReadStatus {
  if (book.readProgress?.completed) return ReadStatus.READ
  if (book.readProgress?.completed === false) return ReadStatus.IN_PROGRESS
  return ReadStatus.UNREAD
}

export function getReadProgressPercentage (book: BookDto): number {
  if (book.readProgress?.completed) return 100
  if (book.readProgress?.completed === false) {
    return book.readProgress?.page / book.media.pagesCount * 100
  }
  return 0
}
