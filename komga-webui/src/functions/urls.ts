const baseURL = process.env.VUE_APP_KOMGA_API_URL ? process.env.VUE_APP_KOMGA_API_URL : window.location.origin

export function bookThumbnailUrl (bookId: number): string {
  return `${baseURL}/api/v1/books/${bookId}/thumbnail`
}

export function bookFileUrl (bookId: number): string {
  return `${baseURL}/api/v1/books/${bookId}/file`
}

export function bookPageUrl (bookId: number, page: number, convertTo?: string): string {
  return `${baseURL}/api/v1/books/${bookId}/pages/${page}` + convertTo ? `?convert=${convertTo}` : ''
}

export function bookPageThumbnailUrl (bookId: number, page: number): string {
  return `${baseURL}/api/v1/books/${bookId}/pages/${page}/thumbnail`
}

export function seriesThumbnailUrl (seriesId: number): string {
  return `${baseURL}/api/v1/series/${seriesId}/thumbnail`
}
