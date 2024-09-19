import {Context} from '@/types/context'
import {CopyMode} from '@/types/enum-books'

export interface BookDto {
  id: string,
  seriesId: string,
  seriesTitle: string,
  libraryId: string,
  name: string,
  url: string,
  number: number,
  created: Date,
  lastModified: Date,
  sizeBytes: number,
  size: string,
  media: MediaDto,
  metadata: BookMetadataDto,
  readProgress?: ReadProgressDto,
  deleted: boolean,
  oneshot: boolean,

  // custom fields
  context: Context
}

export interface MediaDto {
  status: string,
  mediaType: string,
  pagesCount: number,
  comment: string,
  mediaProfile: string,
  epubDivinaCompatible: boolean,
  epubIsKepub: boolean,
}

export interface PageDto {
  number: number,
  fileName: string,
  mediaType: string,
  width?: number,
  height?: number,
  sizeBytes?: number,
  size: string,
}

export interface PageDtoWithUrl {
  number: number,
  fileName: string,
  mediaType: string,
  width?: number,
  height?: number,
  sizeBytes?: number,
  size: string,
  url: string,
}

export interface BookMetadataDto {
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
  releaseDate?: string,
  releaseDateLock: boolean,
  authors: AuthorDto[],
  authorsLock: boolean,
  tags: string[],
  tagsLock: boolean,
  isbn: string,
  isbnLock: boolean
  links?: WebLinkDto[],
  linksLock?: boolean
}

export interface ReadProgressDto {
  page: number,
  completed: boolean,
  readDate: Date,
  created: Date,
  lastModified: Date
}

export interface BookMetadataUpdateDto {
  title?: string,
  titleLock?: boolean,
  summary?: string,
  summaryLock?: boolean,
  number?: string,
  numberLock?: boolean,
  numberSort?: number,
  numberSortLock?: boolean,
  releaseDate?: string,
  releaseDateLock?: boolean,
  authors?: AuthorDto[],
  authorsLock?: boolean,
  tags?: string[],
  tagsLock?: boolean
  isbn?: string,
  isbnLock?: boolean,
  links?: WebLinkDto[],
  linksLock?: boolean
}

export interface BookMetadataUpdateBatchDto {
  [bookId: string]: BookMetadataUpdateDto
}

export interface AuthorDto {
  name: string,
  role: string
}

export interface WebLinkDto {
  label: string,
  url: string
}

export interface ReadProgressUpdateDto {
  page?: number,
  completed?: boolean
}

export interface BookFormat {
  type: string,
  color: string
}

export interface BookImportBatchDto {
  books: BookImportDto[],
  copyMode: CopyMode,
}

export interface BookImportDto {
  sourceFile: string,
  seriesId: string,
  upgradeBookId?: string,
  destinationName?: string,
}

export interface BookThumbnailDto {
  id: string,
  bookId: string,
  type: string,
  selected: boolean,
  mediaType: string,
  fileSize: number,
  width: number,
  height: number,
}
