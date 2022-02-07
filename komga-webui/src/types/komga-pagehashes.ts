import {PageHashAction} from '@/types/enum-pagehashes'

export interface PageHashDto {
  hash: string,
  mediaType: string,
  size?: number,
}

export interface PageHashUnknownDto extends PageHashDto {
  matchCount: number,
}

export interface PageHashMatchDto {
  bookId: string,
  url: string,
  pageNumber: number,
  fileName: string,
}

export interface PageHashCreationDto {
  hash: string,
  mediaType: string,
  size?: number,
  action: PageHashAction,
}

export interface PageHashKnownDto extends PageHashDto {
  action: PageHashAction
  deleteCount: number,
  createdDate: string,
  lastModifiedDate: string,
}
