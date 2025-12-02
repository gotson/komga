import { API_BASE_URL } from '@/api/base'

export function seriesThumbnailUrl(seriesId?: string): string | undefined {
  if (seriesId) return `${API_BASE_URL}/api/v1/series/${seriesId}/thumbnail`
  return undefined
}

export function bookThumbnailUrl(bookId?: string): string | undefined {
  if (bookId) return `${API_BASE_URL}/api/v1/books/${bookId}/thumbnail`
  return undefined
}

export function bookPageThumbnailUrl(bookId?: string, page?: number): string | undefined {
  if (bookId && page) return `${API_BASE_URL}/api/v1/books/${bookId}/pages/${page}/thumbnail`
  return undefined
}

export function pageHashKnownThumbnailUrl(hash?: string): string | undefined {
  if (hash) return `${API_BASE_URL}/api/v1/page-hashes/${hash}/thumbnail`
  return undefined
}

export function pageHashUnknownThumbnailUrl(hash?: string): string | undefined {
  if (hash) return `${API_BASE_URL}/api/v1/page-hashes/unknown/${hash}/thumbnail`
  return undefined
}
