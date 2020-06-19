interface CollectionDto {
  id: number,
  name: string,
  ordered: boolean,
  filtered: boolean,
  seriesIds: number[],
  createdDate: string,
  lastModifiedDate: string
}

interface CollectionCreationDto {
  name: string,
  ordered: boolean,
  seriesIds: number[]
}

interface CollectionUpdateDto {
  name?: string,
  ordered?: boolean,
  seriesIds?: number[]
}
