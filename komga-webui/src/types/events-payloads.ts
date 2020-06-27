interface EventBookChanged {
  id: number,
  seriesId: number
}

interface EventSeriesChanged {
  id: number,
  libraryId: number
}

interface EventCollectionDeleted {
  id: number
}

interface EventLibraryDeleted {
  id: number
}
