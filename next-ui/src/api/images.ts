import { ApiBaseUrl } from '@/api/base'

export function seriesPosterUrl(seriesId?: string, version?: number): string | undefined {
  if (seriesId)
    return `${ApiBaseUrl.noSlash}/api/v1/series/${seriesId}/thumbnail${version ? '?t=' + version : ''}`
  return undefined
}

export function bookPosterUrl(bookId?: string, version?: number): string | undefined {
  if (bookId)
    return `${ApiBaseUrl.noSlash}/api/v1/books/${bookId}/thumbnail${version ? '?t=' + version : ''}`
  return undefined
}

export function collectionPosterUrl(collectionId?: string, version?: number): string | undefined {
  if (collectionId)
    return `${ApiBaseUrl.noSlash}/api/v1/collections/${collectionId}/thumbnail${version ? '?t=' + version : ''}`
  return undefined
}

export function readListPosterUrl(readListId?: string, version?: number): string | undefined {
  if (readListId)
    return `${ApiBaseUrl.noSlash}/api/v1/readlists/${readListId}/thumbnail${version ? '?t=' + version : ''}`
  return undefined
}

export function bookPageThumbnailUrl(bookId?: string, page?: number): string | undefined {
  if (bookId && page) return `${ApiBaseUrl.noSlash}/api/v1/books/${bookId}/pages/${page}/thumbnail`
  return undefined
}

export function pageHashKnownThumbnailUrl(hash?: string): string | undefined {
  if (hash) return `${ApiBaseUrl.noSlash}/api/v1/page-hashes/${hash}/thumbnail`
  return undefined
}

export function pageHashUnknownThumbnailUrl(hash?: string): string | undefined {
  if (hash) return `${ApiBaseUrl.noSlash}/api/v1/page-hashes/unknown/${hash}/thumbnail`
  return undefined
}
