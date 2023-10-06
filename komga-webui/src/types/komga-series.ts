import {AuthorDto, BookDto, WebLinkDto} from '@/types/komga-books'
import {Context} from '@/types/context'

export interface SeriesDto {
  id: string,
  libraryId: string,
  name: string,
  url: string,
  created: Date,
  lastModified: Date,
  booksCount: number,
  booksReadCount: number,
  booksUnreadCount: number,
  booksInProgressCount: number,
  metadata: SeriesMetadataDto,
  booksMetadata: SeriesBooksMetadataDto,
  deleted: boolean,
  oneshot: boolean,

  // custom fields
  context: Context
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
  links: WebLinkDto[],
  linksLock: boolean,
  alternateTitles: AlternateTitleDto[],
  alternateTitlesLock: boolean,
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
  links?: WebLinkDto[],
  linksLock?: boolean,
  alternateTitles?: AlternateTitleDto[],
  alternateTitlesLock?: boolean,
}

export interface GroupCountDto {
  group: string,
  count: number,
}

export interface SeriesThumbnailDto {
  id: string,
  seriesId: string,
  type: string,
  selected: boolean,
  mediaType: string,
  fileSize: number,
  width: number,
  height: number,
}

export interface AlternateTitleDto {
  label: string,
  title: string
}

export interface Oneshot {
  series: SeriesDto,
  book: BookDto,
}
