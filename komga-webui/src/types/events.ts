export const BOOK_CHANGED = 'book-changed'
export const SERIES_CHANGED = 'series-changed'
export const COLLECTION_DELETED = 'collection-deleted'
export const COLLECTION_CHANGED = 'collection-changed'
export const READLIST_DELETED = 'readlist-deleted'
export const READLIST_CHANGED = 'readlist-changed'
export const LIBRARY_ADDED = 'library-added'
export const LIBRARY_CHANGED = 'library-changed'
export const LIBRARY_DELETED = 'library-deleted'

export function bookToEventBookChanged (book: BookDto): EventBookChanged {
  return {
    id: book.id,
    seriesId: book.seriesId,
  } as EventBookChanged
}

export function seriesToEventSeriesChanged (series: SeriesDto): EventSeriesChanged {
  return {
    id: series.id,
    libraryId: series.libraryId,
  } as EventSeriesChanged
}

export function collectionToEventCollectionChanged (collection: CollectionDto): EventCollectionChanged {
  return {
    id: collection.id,
  } as EventCollectionChanged
}

export function collectionToEventCollectionDeleted (collection: CollectionDto): EventCollectionDeleted {
  return {
    id: collection.id,
  } as EventCollectionDeleted
}

export function readListToEventReadListChanged (readList: ReadListDto): EventReadListChanged {
  return {
    id: readList.id,
  } as EventReadListChanged
}

export function readListToEventReadListDeleted (readList: ReadListDto): EventReadListDeleted {
  return {
    id: readList.id,
  } as EventReadListDeleted
}

export function libraryToEventLibraryAdded (library: LibraryDto): EventLibraryAdded {
  return {
    id: library.id,
  } as EventLibraryAdded
}

export function libraryToEventLibraryChanged (library: LibraryDto): EventLibraryChanged {
  return {
    id: library.id,
  } as EventLibraryChanged
}

export function libraryToEventLibraryDeleted (library: LibraryDto): EventLibraryDeleted {
  return {
    id: library.id,
  } as EventLibraryDeleted
}
