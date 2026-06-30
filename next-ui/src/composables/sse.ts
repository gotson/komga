import { createGlobalState, useEventSource, watchImmediate } from '@vueuse/core'
import { ApiBaseUrl } from '@/api/base'
import { logger } from '@/services/logtape'
import { useAppStore } from '@/stores/app'
import * as v from 'valibot'
import { entitiesChanged, entityChanged } from '@/colada/cache'
import { QUERY_KEYS_BOOKS } from '@/colada/books'
import { QUERY_KEYS_SERIES } from '@/colada/series'
import { QUERY_KEYS_COLLECTIONS } from '@/colada/collections'
import { QUERY_KEYS_READLIST } from '@/colada/readlists'
import { useCurrentUser, userLoggedOut } from '@/colada/users'
import { QUERY_KEYS_LIBRARIES } from '@/colada/libraries'
import { useMessagesStore } from '@/stores/messages'
import { defineMessage } from 'vue-intl'
import { useErrorCodeFormatter } from '@/composables/errorCodeFormatter'
const namedEvents = [
  'LibraryAdded',
  'LibraryChanged',
  'LibraryDeleted',
  'SeriesAdded',
  'SeriesChanged',
  'SeriesDeleted',
  'BookAdded',
  'BookChanged',
  'BookDeleted',
  'BookImported',
  'CollectionAdded',
  'CollectionChanged',
  'CollectionDeleted',
  'ReadListAdded',
  'ReadListChanged',
  'ReadListDeleted',
  'ReadProgressChanged',
  'ReadProgressDeleted',
  'ReadProgressSeriesChanged',
  'ReadProgressSeriesDeleted',
  'ThumbnailBookAdded',
  'ThumbnailBookDeleted',
  'ThumbnailSeriesAdded',
  'ThumbnailSeriesDeleted',
  'ThumbnailReadListAdded',
  'ThumbnailReadListDeleted',
  'ThumbnailSeriesCollectionAdded',
  'ThumbnailSeriesCollectionDeleted',
  'TaskQueueStatus',
  'SessionExpired',
]

const SSELibrarySchema = v.object({
  event: v.picklist(['LibraryAdded', 'LibraryChanged', 'LibraryDeleted']),
  data: v.object({
    libraryId: v.string(),
  }),
})

const SSESeriesSchema = v.object({
  event: v.picklist(['SeriesAdded', 'SeriesChanged', 'SeriesDeleted']),
  data: v.object({
    seriesId: v.string(),
    libraryId: v.string(),
  }),
})

const SSEBookSchema = v.object({
  event: v.picklist(['BookAdded', 'BookChanged', 'BookDeleted']),
  data: v.object({
    bookId: v.string(),
    seriesId: v.string(),
    libraryId: v.string(),
  }),
})

const SSEBookImportedSchema = v.object({
  event: v.picklist(['BookImported']),
  data: v.object({
    bookId: v.nullable(v.string()),
    sourceFile: v.string(),
    success: v.boolean(),
    message: v.nullable(v.string()),
  }),
})

const SSEReadListSchema = v.object({
  event: v.picklist(['ReadListAdded', 'ReadListChanged', 'ReadListDeleted']),
  data: v.object({
    readListId: v.string(),
    bookIds: v.array(v.string()),
  }),
})

const SSECollectionSchema = v.object({
  event: v.picklist(['CollectionAdded', 'CollectionChanged', 'CollectionDeleted']),
  data: v.object({
    collectionId: v.string(),
    seriesIds: v.array(v.string()),
  }),
})

const SSEReadProgressSchema = v.object({
  event: v.picklist(['ReadProgressChanged', 'ReadProgressDeleted']),
  data: v.object({
    bookId: v.string(),
    userId: v.string(),
  }),
})

const SSEReadProgressSeriesSchema = v.object({
  event: v.picklist(['ReadProgressSeriesChanged', 'ReadProgressSeriesDeleted']),
  data: v.object({
    seriesId: v.string(),
    userId: v.string(),
  }),
})

const SSESessionExpiredSchema = v.object({
  event: v.picklist(['SessionExpired']),
  data: v.object({
    userId: v.string(),
  }),
})

const SSETaskQueueSchema = v.object({
  event: v.picklist(['TaskQueueStatus']),
  data: v.object({
    count: v.number(),
    countByType: v.record(v.string(), v.number()),
  }),
})

//TODO: thumbnail events

const SSEEventSchema = v.variant('event', [
  SSELibrarySchema,
  SSESeriesSchema,
  SSEBookSchema,
  SSEBookImportedSchema,
  SSEReadListSchema,
  SSECollectionSchema,
  SSEReadProgressSchema,
  SSEReadProgressSeriesSchema,
  SSESessionExpiredSchema,
  SSETaskQueueSchema,
])

/**
 * Singleton composable for handling SSE.
 * Browsers limit the number HTTP/1 SSE connections to 6, so we use:
 * - the Web Locks API to elect a leader among the multiple open tabs
 * - a BroadcastChannel to forward SSE events to follower tabs
 *
 * In case the Web Locks API is not available, each tab will attempt to connect to SSE.
 *
 * As the SSE is behind auth, it automatically connects/disconnects by watching the auth state.
 */
export const useSSE = createGlobalState(() => {
  const url = ApiBaseUrl.noSlash + '/sse/v1/events'
  const localEvent = ref<{ event?: string; data?: object }>({})
  const isLeader = ref(false)
  const appStore = useAppStore()
  const messagesStore = useMessagesStore()
  const { convertErrorCodes } = useErrorCodeFormatter()

  const { isAuthenticated } = useCurrentUser()

  let releaseLock: (() => void) | null = null
  let closeEventSource: (() => void) | null = null
  let unwatchSSE: (() => void) | null = null
  let broadcastChannel: BroadcastChannel | null = null
  let lockAbortController: AbortController | null = null

  function startSSEConnection(enableBroadcast: boolean) {
    const { data, event, close } = useEventSource(url, namedEvents, {
      withCredentials: true,
      serializer: {
        read: (rawData) => (rawData ? JSON.parse(rawData) : {}),
      },
      autoReconnect: {
        retries: 3,
        delay: 1000,
        onFailed() {
          logger.error('Failed to connect EventSource after 3 retries')
          appStore.sseUnavailable = true
        },
      },
    })

    closeEventSource = close

    unwatchSSE = watch(data, (newData) => {
      const msg = { event: event.value ?? undefined, data: newData }
      logger.debug('SSE received from EventSource', msg)
      // publish locally
      localEvent.value = msg
      // publish to followers
      if (enableBroadcast) broadcastChannel?.postMessage(msg)
    })
  }

  function connect() {
    // check for Secure Context / Web Locks API support
    if ('locks' in navigator) {
      // prevent duplicate connections
      if (broadcastChannel) return

      // setup broadcast channel for multi-tabs connectivity
      broadcastChannel = new BroadcastChannel('komga-sse-data-sync')
      broadcastChannel.onmessage = (msg) => {
        logger.debug('SSE received from BroadcastChannel', msg.data)
        if (!isLeader.value) {
          localEvent.value = msg.data
        }
      }

      // setup AbortController to cancel pending locks
      lockAbortController = new AbortController()

      // attempt to get the leader lock
      void navigator.locks
        .request('komga-sse-leader-lock', { signal: lockAbortController.signal }, async () => {
          // safety check
          if (!isAuthenticated.value) return

          // if we get the lock, we are leader
          logger.info('SSE lock obtained, setting up EventSource subscription')
          isLeader.value = true

          startSSEConnection(true)

          // Hold the lock indefinitely until releaseLock is called during disconnect()
          return new Promise<void>((resolve) => {
            releaseLock = resolve
          })
        })
        .catch((err) => {
          if (err.name === 'AbortError') {
            logger.info('SSE lock request aborted due to logout')
          } else {
            logger.error('SSE lock request failed', err)
          }
        })
        .finally(() => {
          // Cleanup fallback when the lock is released or fails
          isLeader.value = false
          releaseLock = null
          closeEventSource = null
        })
    } else {
      logger.warn('Web Locks API unavailable. Falling back to direct EventSource connection.')

      // local processing, everyone is a leader
      isLeader.value = true

      startSSEConnection(false)
    }
  }

  function disconnect() {
    logger.info('Disconnected SSE and BroadcastChannel')

    // Abort the lock request if we are a follower waiting in the queue
    if (lockAbortController) {
      lockAbortController.abort()
      lockAbortController = null
    }
    if (unwatchSSE) {
      unwatchSSE()
      unwatchSSE = null
    }
    // Close the actual SSE connection if we are the leader
    if (closeEventSource) {
      closeEventSource()
      closeEventSource = null
    }
    // Resolve the inner Promise to release the navigator lock for the next tab
    if (releaseLock) {
      releaseLock()
      releaseLock = null
    }
    // Close the broadcast channel
    if (broadcastChannel) {
      broadcastChannel.close()
      broadcastChannel = null
    }

    // Ensure we reset leader status
    isLeader.value = false
  }

  // Watch authentication state to trigger connect/disconnect automatically
  watchImmediate(isAuthenticated, (authenticated) => {
    if (authenticated) connect()
    else disconnect()
  })

  // process events
  watch(localEvent, (newEvent) => {
    logger.debug('SSE to process', newEvent)
    const result = v.safeParse(SSEEventSchema, newEvent)
    if (!result.success) {
      logger.error('Failed to parse event', newEvent)
      return
    }

    const event = result.output
    switch (event.event) {
      case 'LibraryAdded':
      case 'LibraryChanged':
      case 'LibraryDeleted':
        entitiesChanged(QUERY_KEYS_LIBRARIES.root)
        break
      case 'SeriesAdded':
      case 'SeriesChanged':
      case 'SeriesDeleted':
        entityChanged(QUERY_KEYS_SERIES.root, event.data.seriesId)
        break
      case 'BookAdded':
      case 'BookChanged':
      case 'BookDeleted':
        entityChanged(QUERY_KEYS_BOOKS.root, event.data.bookId)
        break
      case 'BookImported':
        if (event.data.success && event.data.bookId)
          messagesStore.messages.push({
            titleMessage: defineMessage({
              description: 'Book imported notification: import success: title',
              defaultMessage: 'Book imported',
              id: 'sPScum',
            }),
            message: event.data.sourceFile,
            action: {
              to: { name: '/book/[id]', params: { id: event.data.bookId } },
              label: defineMessage({
                description: 'Book imported notification: button text to navigate to book',
                defaultMessage: 'Open',
                id: 'JwBlXP',
              }),
            },
          })
        else
          messagesStore.messages.push({
            titleMessage: defineMessage({
              description: 'Book imported notification: import failure: title',
              defaultMessage: 'Book import failed',
              id: 'vkeTYG',
            }),
            message: `${convertErrorCodes(event.data.message)}\n${event.data.sourceFile}`,
            color: 'error',
          })
        break
      case 'ReadListAdded':
      case 'ReadListChanged':
      case 'ReadListDeleted':
        entityChanged(QUERY_KEYS_READLIST.root, event.data.readListId)
        break
      case 'CollectionAdded':
      case 'CollectionChanged':
      case 'CollectionDeleted':
        entityChanged(QUERY_KEYS_COLLECTIONS.root, event.data.collectionId)
        break
      case 'ReadProgressChanged':
      case 'ReadProgressDeleted':
        entityChanged(QUERY_KEYS_BOOKS.root, event.data.bookId)
        break
      case 'ReadProgressSeriesChanged':
      case 'ReadProgressSeriesDeleted':
        entityChanged(QUERY_KEYS_SERIES.root, event.data.seriesId)
        break
      case 'SessionExpired':
        userLoggedOut()
        break
      case 'TaskQueueStatus':
        //TODO: handle
        break
    }
  })
})
