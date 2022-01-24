interface ReadListDto {
  id: string,
  name: string,
  summary: string,
  filtered: boolean,
  bookIds: string[],
  createdDate: string,
  lastModifiedDate: string
}

interface ReadListCreationDto {
  name: string,
  summary?: string,
  bookIds: string[]
}

interface ReadListUpdateDto {
  name?: string,
  summary?: string,
  bookIds?: string[]
}

interface ReadListRequestResultDto {
  readList?: ReadListDto,
  unmatchedBooks: ReadListRequestResultBookDto[],
  errorCode: string,
  requestName: string,
}

interface ReadListRequestResultBookDto {
  book: ReadListRequestBookDto,
  errorCode: string,
}

interface ReadListRequestBookDto {
  series: string,
  number: string,
}

interface ReadListThumbnailDto {
  id: string,
  readListId: string,
  type: string,
  selected: boolean
}
