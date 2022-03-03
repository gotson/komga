import urls from '@/functions/urls'
import {
  BOOK_ADDED,
  BOOK_CHANGED,
  BOOK_DELETED,
  BOOK_IMPORTED,
  COLLECTION_ADDED,
  COLLECTION_CHANGED,
  COLLECTION_DELETED,
  LIBRARY_ADDED,
  LIBRARY_CHANGED,
  LIBRARY_DELETED,
  READLIST_ADDED,
  READLIST_CHANGED,
  READLIST_DELETED,
  READPROGRESS_CHANGED,
  READPROGRESS_DELETED,
  READPROGRESS_SERIES_CHANGED,
  READPROGRESS_SERIES_DELETED,
  SERIES_ADDED,
  SERIES_CHANGED,
  SERIES_DELETED, SESSION_EXPIRED,
  THUMBNAILBOOK_ADDED,
  THUMBNAILBOOK_DELETED,
  THUMBNAILCOLLECTION_ADDED,
  THUMBNAILCOLLECTION_DELETED,
  THUMBNAILREADLIST_ADDED,
  THUMBNAILREADLIST_DELETED,
  THUMBNAILSERIES_ADDED,
  THUMBNAILSERIES_DELETED,
} from '@/types/events'
import Vue from 'vue'
import {TaskQueueSseDto} from '@/types/komga-sse'

const API_SSE = '/sse/v1/events'

export default class KomgaSseService {
  private eventSource: EventSource | undefined
  private eventHub: Vue
  private store: any

  constructor(eventHub: Vue, store: any) {
    this.eventHub = eventHub
    this.store = store

    this.eventHub.$watch(
      () => this.store.getters.authenticated,
      (val) => {
        if (val) this.connect()
        else this.disconnect()
      })
  }

  connect() {
    this.eventSource = new EventSource(urls.originNoSlash + API_SSE, {withCredentials: true})

    // Libraries
    this.eventSource.addEventListener('LibraryAdded', (event: any) => this.emit(LIBRARY_ADDED, event))
    this.eventSource.addEventListener('LibraryChanged', (event: any) => this.emit(LIBRARY_CHANGED, event))
    this.eventSource.addEventListener('LibraryDeleted', (event: any) => this.emit(LIBRARY_DELETED, event))

    // Series
    this.eventSource.addEventListener('SeriesAdded', (event: any) => this.emit(SERIES_ADDED, event))
    this.eventSource.addEventListener('SeriesChanged', (event: any) => this.emit(SERIES_CHANGED, event))
    this.eventSource.addEventListener('SeriesDeleted', (event: any) => this.emit(SERIES_DELETED, event))

    // Books
    this.eventSource.addEventListener('BookAdded', (event: any) => this.emit(BOOK_ADDED, event))
    this.eventSource.addEventListener('BookChanged', (event: any) => this.emit(BOOK_CHANGED, event))
    this.eventSource.addEventListener('BookDeleted', (event: any) => this.emit(BOOK_DELETED, event))

    this.eventSource.addEventListener('BookImported', (event: any) => this.emit(BOOK_IMPORTED, event))

    // Collections
    this.eventSource.addEventListener('CollectionAdded', (event: any) => this.emit(COLLECTION_ADDED, event))
    this.eventSource.addEventListener('CollectionChanged', (event: any) => this.emit(COLLECTION_CHANGED, event))
    this.eventSource.addEventListener('CollectionDeleted', (event: any) => this.emit(COLLECTION_DELETED, event))

    // Read Lists
    this.eventSource.addEventListener('ReadListAdded', (event: any) => this.emit(READLIST_ADDED, event))
    this.eventSource.addEventListener('ReadListChanged', (event: any) => this.emit(READLIST_CHANGED, event))
    this.eventSource.addEventListener('ReadListDeleted', (event: any) => this.emit(READLIST_DELETED, event))

    // Read Progress
    this.eventSource.addEventListener('ReadProgressChanged', (event: any) => this.emit(READPROGRESS_CHANGED, event))
    this.eventSource.addEventListener('ReadProgressDeleted', (event: any) => this.emit(READPROGRESS_DELETED, event))
    this.eventSource.addEventListener('ReadProgressSeriesChanged', (event: any) => this.emit(READPROGRESS_SERIES_CHANGED, event))
    this.eventSource.addEventListener('ReadProgressSeriesDeleted', (event: any) => this.emit(READPROGRESS_SERIES_DELETED, event))

    // Thumbnails
    this.eventSource.addEventListener('ThumbnailBookAdded', (event: any) => this.emit(THUMBNAILBOOK_ADDED, event))
    this.eventSource.addEventListener('ThumbnailBookDeleted', (event: any) => this.emit(THUMBNAILBOOK_DELETED, event))

    this.eventSource.addEventListener('ThumbnailSeriesAdded', (event: any) => this.emit(THUMBNAILSERIES_ADDED, event))
    this.eventSource.addEventListener('ThumbnailSeriesDeleted', (event: any) => this.emit(THUMBNAILSERIES_DELETED, event))

    this.eventSource.addEventListener('ThumbnailReadListAdded', (event: any) => this.emit(THUMBNAILREADLIST_ADDED, event))
    this.eventSource.addEventListener('ThumbnailReadListDeleted', (event: any) => this.emit(THUMBNAILREADLIST_DELETED, event))

    this.eventSource.addEventListener('ThumbnailSeriesCollectionAdded', (event: any) => this.emit(THUMBNAILCOLLECTION_ADDED, event))
    this.eventSource.addEventListener('ThumbnailSeriesCollectionDeleted', (event: any) => this.emit(THUMBNAILCOLLECTION_DELETED, event))

    this.eventSource.addEventListener('TaskQueueStatus', (event: any) => this.updateTaskCount(event))

    this.eventSource.addEventListener('SessionExpired', (event: any) => this.emit(SESSION_EXPIRED, event))
  }

  disconnect() {
    this.eventSource?.close()
  }

  private emit(name: string, event: any) {
    this.eventHub.$emit(name, JSON.parse(event.data))
  }

  private updateTaskCount(event: any) {
    const data = JSON.parse(event.data) as TaskQueueSseDto
    this.store.commit('setTaskCount', data)
  }
}
