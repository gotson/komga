interface EventBookChanged {
  id: string,
  seriesId: string
}

interface EventSeriesChanged {
  id: string,
  libraryId: string
}

interface EventCollectionChanged {
  id: string
}

interface EventCollectionDeleted {
  id: string
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
