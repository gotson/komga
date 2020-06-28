export const BOOK_CHANGED = 'book-changed'
export const SERIES_CHANGED = 'series-changed'
export const COLLECTION_DELETED = 'collection-deleted'
export const COLLECTION_CHANGED = 'collection-changed'
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

export function collectionToEventCollectionDeleted (collection: CollectionDto): EventCollectionDeleted {
  return {
    id: collection.id,
  } as EventCollectionDeleted
}

export function libraryToEventLibraryDeleted (library: LibraryDto): EventLibraryDeleted {
  return {
    id: library.id,
  } as EventLibraryDeleted
}
