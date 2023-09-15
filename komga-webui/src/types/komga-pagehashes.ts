import {PageHashAction} from '@/types/enum-pagehashes'

export interface PageHashDto {
  hash: string,
  size?: number,
  matchCount: number,
}

export interface PageHashUnknownDto extends PageHashDto {
}

export interface PageHashMatchDto {
  bookId: string,
  url: string,
  pageNumber: number,
  fileName: string,
  fileSize: number,
  mediaType: string,
}

export interface PageHashCreationDto {
  hash: string,
  size?: number,
  action: PageHashAction,
}

export interface PageHashKnownDto extends PageHashDto {
  action: PageHashAction
  deleteCount: number,
  createdDate: Date,
  lastModifiedDate: Date,
}
