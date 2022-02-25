import {AuthorDto} from '@/types/komga-books'

export interface SeriesDto {
  id: string,
  libraryId: string,
  name: string,
  url: string,
  created: string,
  lastModified: string,
  booksCount: number,
  booksReadCount: number,
  booksUnreadCount: number,
  booksInProgressCount: number,
  metadata: SeriesMetadataDto,
  booksMetadata: SeriesBooksMetadataDto,
  deleted: boolean,
}

export interface SeriesMetadataDto {
  status: string,
  statusLock: boolean,
  created: string,
  lastModified: string,
  title: string,
  titleLock: boolean,
  titleSort: string,
  titleSortLock: boolean,
  summary: string,
  summaryLock: boolean,
  readingDirection: string,
  readingDirectionLock: boolean,
  publisher: string,
  publisherLock: boolean,
  ageRating?: number,
  ageRatingLock: boolean,
  language: string,
  languageLock: boolean,
  genres: string[],
  genresLock: boolean,
  tags: string[],
  tagsLock: boolean,
  totalBookCount?: number,
  totalBookCountLock: boolean,
  sharingLabels: string[],
  sharingLabelsLock: boolean,
}

export interface SeriesBooksMetadataDto {
  created: string,
  lastModified: string
  authors: AuthorDto[],
  tags: string[],
  releaseDate: string,
  summary: string,
  summaryNumber: string,
}

export interface SeriesMetadataUpdateDto {
  status?: string,
  statusLock?: boolean,
  title?: string,
  titleLock?: boolean,
  titleSort?: string,
  titleSortLock?: boolean,
  summary?: string,
  summaryLock?: boolean,
  readingDirection?: string,
  readingDirectionLock?: boolean,
  publisher?: string,
  publisherLock?: boolean,
  ageRating?: number,
  ageRatingLock?: boolean,
  language?: string,
  languageLock?: boolean,
  genres?: string[],
  genresLock?: boolean,
  tags?: string[],
  tagsLock?: boolean,
  totalBookCount?: number,
  totalBookCountLock: boolean,
  sharingLabels?: string[],
  sharingLabelsLock: boolean,
}

export interface GroupCountDto {
  group: string,
  count: number,
}

export interface SeriesThumbnailDto {
  id: string,
  seriesId: string,
  type: string,
  selected: boolean
}
