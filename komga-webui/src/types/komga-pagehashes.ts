export interface PageHashUnknownDto {
  hash: string,
  mediaType: string,
  sizeBytes?: number,
  size?: string,
  totalSize?: string,
  matchCount: number,
}

export interface PageHashMatchDto {
  bookId: string,
  url: string,
  pageNumber: number,
  fileName: string,
}
