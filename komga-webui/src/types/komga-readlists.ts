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

export interface ReadListRequestResultDto {
  readList?: ReadListDto,
  unmatchedBooks: ReadListRequestResultBookDto[],
  errorCode: string,
  requestName: string,
}

export interface ReadListRequestResultBookDto {
  book: ReadListRequestBookDto,
  errorCode: string,
}

export interface ReadListRequestBookDto {
  series: string,
  number: string,
}

export interface ReadListThumbnailDto {
  id: string,
  readListId: string,
  type: string,
  selected: boolean
}
