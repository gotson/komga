const fullUrl = process.env.VUE_APP_KOMGA_API_URL
  ? process.env.VUE_APP_KOMGA_API_URL
  : window.location.origin + window.resourceBaseUrl
const baseUrl = process.env.NODE_ENV === 'production' ? window.resourceBaseUrl : '/'

const urls = {
  origin: !fullUrl.endsWith('/') ? `${fullUrl}/` : fullUrl,
  originNoSlash: fullUrl.endsWith('/') ? fullUrl.slice(0, -1) : fullUrl,
  base: !baseUrl.endsWith('/') ? `${baseUrl}/` : baseUrl,
  baseNoSlash: baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl,
} as Urls

export default urls

export function bookThumbnailUrl (bookId: string): string {
  return `${urls.originNoSlash}/api/v1/books/${bookId}/thumbnail`
}

export function bookFileUrl (bookId: string): string {
  return `${urls.originNoSlash}/api/v1/books/${bookId}/file`
}

export function bookPageUrl (bookId: string, page: number, convertTo?: string): string {
  let url = `${urls.originNoSlash}/api/v1/books/${bookId}/pages/${page}`
  if (convertTo) {
    url += `?convert=${convertTo}`
  }
  return url
}

export function bookPageThumbnailUrl (bookId: string, page: number): string {
  return `${urls.originNoSlash}/api/v1/books/${bookId}/pages/${page}/thumbnail`
}

export function seriesThumbnailUrl (seriesId: string): string {
  return `${urls.originNoSlash}/api/v1/series/${seriesId}/thumbnail`
}

export function collectionThumbnailUrl (collectionId: string): string {
  return `${urls.originNoSlash}/api/v1/collections/${collectionId}/thumbnail`
}
