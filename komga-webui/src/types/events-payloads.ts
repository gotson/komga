interface EventBookChanged {
  id: number,
  seriesId: number
}

interface EventSeriesChanged {
  id: number,
  libraryId: number
}

interface EventCollectionChanged {
  id: number
}

interface EventCollectionDeleted {
  id: number
}

interface EventLibraryAdded {
  id: number
}

interface EventLibraryChanged {
  id: number
}

interface EventLibraryDeleted {
  id: number
}
