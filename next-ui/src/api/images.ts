export function seriesThumbnailUrl(seriesId?: string): string | undefined {
  if (seriesId) return `${import.meta.env.VITE_KOMGA_API_URL}/api/v1/series/${seriesId}/thumbnail`
  return undefined
}

export function bookThumbnailUrl(bookId?: string): string | undefined {
  if (bookId) return `${import.meta.env.VITE_KOMGA_API_URL}/api/v1/books/${bookId}/thumbnail`
  return undefined
}

export function pageHashKnownThumbnailUrl(hash?: string): string | undefined {
  if (hash) return `${import.meta.env.VITE_KOMGA_API_URL}/api/v1/page-hashes/${hash}/thumbnail`
  return undefined
}
