export interface ReadListDto {
  id: string,
  name: string,
  summary: string,
  ordered: boolean,
  filtered: boolean,
  bookIds: string[],
  createdDate: string,
  lastModifiedDate: string
}

export interface ReadListCreationDto {
  name: string,
  summary?: string,
  ordered?: boolean,
  bookIds: string[]
}

export interface ReadListUpdateDto {
  name?: string,
  summary?: string,
  ordered?: boolean,
  bookIds?: string[]
}

export interface ReadListRequestBookDto {
  series: string[],
  number: string,
}

export interface ReadListThumbnailDto {
  id: string,
  readListId: string,
  type: string,
  selected: boolean
}

export interface ReadListRequestMatchDto {
  readListMatch: ReadListMatchDto,
  requests: ReadListRequestBookMatchesDto[],
  errorCode: string,
}

export interface ReadListMatchDto {
  name: string,
  errorCode: string,
}

export interface ReadListRequestBookMatchesDto {
  request: ReadListRequestBookDto,
  matches: ReadListRequestBookMatchDto[],
}

export interface ReadListRequestBookMatchDto {
  series: ReadListRequestBookMatchSeriesDto,
  books: ReadListRequestBookMatchBookDto[],
}

export interface ReadListRequestBookMatchSeriesDto {
  seriesId: string,
  title: string,
}

export interface ReadListRequestBookMatchBookDto {
  bookId: string,
  number: string,
  title: string,
}
