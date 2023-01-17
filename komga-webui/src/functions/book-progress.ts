import { ReadStatus } from '@/types/enum-books'
import { BookDto } from '@/types/komga-books'

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

export function getPagesLeft (book: BookDto): number {
  if (book.readProgress?.completed === false) {
    return book.media.pagesCount - book.readProgress?.page
  }
  return 0
}
