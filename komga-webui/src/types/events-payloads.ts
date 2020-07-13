interface EventBookChanged {
  id: string,
  seriesId: string
}

interface EventSeriesChanged {
  id: string,
  libraryId: string
}

interface EventCollectionChanged {
  id: number
}

interface EventCollectionDeleted {
  id: number
}

interface EventLibraryAdded {
  id: string
}

interface EventLibraryChanged {
  id: string
}

interface EventLibraryDeleted {
  id: string
}
