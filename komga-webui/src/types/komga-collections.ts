interface CollectionDto {
  id: string,
  name: string,
  ordered: boolean,
  filtered: boolean,
  seriesIds: string[],
  createdDate: string,
  lastModifiedDate: string
}

interface CollectionCreationDto {
  name: string,
  ordered: boolean,
  seriesIds: string[]
}

interface CollectionUpdateDto {
  name?: string,
  ordered?: boolean,
  seriesIds?: string[]
}

interface CollectionThumbnailDto {
  id: string,
  collectionId: string,
  type: string,
  selected: boolean
}
