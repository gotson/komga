interface BookDto {
  id: number,
  seriesId: number,
  name: string,
  url: string,
  number: number,
  lastModified: string,
  sizeBytes: number,
  size: string,
  media: MediaDto
}

interface MediaDto {
  status: string,
  mediaType: string,
  pagesCount: number
}

interface PageDto {
  number: number,
  fileName: string,
  mediaType: string
}

interface BookFormat {
  type: string,
  color: string
}
