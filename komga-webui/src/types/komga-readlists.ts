interface ReadListDto {
  id: string,
  name: string,
  filtered: boolean,
  bookIds: string[],
  createdDate: string,
  lastModifiedDate: string
}

interface ReadListCreationDto {
  name: string,
  bookIds: string[]
}

interface ReadListUpdateDto {
  name?: string,
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
  number: number,
}
