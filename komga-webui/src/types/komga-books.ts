interface BookDto {
  id: number,
  name: string,
  url: string,
  number: number,
  lastModified: string,
  sizeBytes: number,
  size: string,
  metadata: BookMetadataDto,
  seriesId?: number
}

interface BookMetadataDto {
  status: string,
  mediaType: string,
  pagesCount: number
}
