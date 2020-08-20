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
