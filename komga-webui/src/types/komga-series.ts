interface SeriesDto {
  id: number,
  libraryId: number,
  name: string,
  url: string,
  lastModified: string,
  booksCount: number,
  metadata: SeriesMetadata
}

interface SeriesMetadata {
  status: string,
  created: string,
  lastModified: string
}

interface SeriesMetadataUpdateDto {
  status?: string
}
