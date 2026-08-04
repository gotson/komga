import { ApiBaseUrl } from '@/api/base'

export function seriesFileUrl(seriesId?: string): string | undefined {
  if (seriesId) return `${ApiBaseUrl.noSlash}/api/v1/series/${seriesId}/file`
  return undefined
}

export function bookFileUrl(bookId?: string): string | undefined {
  if (bookId) return `${ApiBaseUrl.noSlash}/api/v1/books/${bookId}/file`
  return undefined
}

export function readListFileUrl(readListId?: string): string | undefined {
  if (readListId) return `${ApiBaseUrl.noSlash}/api/v1/readlists/${readListId}/file`
  return undefined
}
