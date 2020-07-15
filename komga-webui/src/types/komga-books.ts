interface BookDto {
  id: string,
  seriesId: string,
  libraryId: string,
  name: string,
  url: string,
  number: number,
  lastModified: string,
  sizeBytes: number,
  size: string,
  media: MediaDto,
  metadata: BookMetadataDto,
  readProgress?: ReadProgressDto
}

interface MediaDto {
  status: string,
  mediaType: string,
  pagesCount: number,
  comment: string
}

interface PageDto {
  number: number,
  fileName: string,
  mediaType: string
}

interface BookMetadataDto {
  created: string,
  lastModified: string,
  title: string,
  titleLock: boolean,
  summary: string,
  summaryLock: boolean,
  number: string,
  numberLock: boolean,
  numberSort: number,
  numberSortLock: boolean,
  readingDirection: string,
  readingDirectionLock: boolean,
  publisher: string,
  publisherLock: boolean,
  ageRating: number,
  ageRatingLock: boolean,
  releaseDate: string,
  releaseDateLock: boolean,
  authors: AuthorDto[],
  authorsLock: boolean,
}

interface ReadProgressDto {
  page: number,
  completed: boolean,
  created: string,
  lastModified: string
}

interface BookMetadataUpdateDto {
  title?: string,
  titleLock?: boolean,
  summary?: string,
  summaryLock?: boolean,
  number?: string,
  numberLock?: boolean,
  numberSort?: number,
  numberSortLock?: boolean,
  readingDirection?: string,
  readingDirectionLock?: boolean,
  publisher?: string,
  publisherLock?: boolean,
  ageRating?: number,
  ageRatingLock?: boolean,
  releaseDate?: string,
  releaseDateLock?: boolean,
  authors?: AuthorDto[],
  authorsLock?: boolean,
}

interface AuthorDto {
  name: string,
  role: string
}

interface ReadProgressUpdateDto {
  page?: number,
  completed?: boolean
}

interface BookFormat {
  type: string,
  color: string
}
