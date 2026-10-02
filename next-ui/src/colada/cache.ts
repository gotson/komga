import * as v from 'valibot'
import { type EntryKey, useQueryCache, type UseQueryEntry } from '@pinia/colada'
import { QUERY_KEYS_BOOKS } from '@/colada/books'
import { QUERY_KEYS_SERIES } from '@/colada/series'
import { QUERY_KEYS_LIBRARIES } from '@/colada/libraries'
import { QUERY_KEYS_READLIST } from '@/colada/readlists'
import { QUERY_KEYS_COLLECTIONS } from '@/colada/collections'

const HasIdSchema = v.looseObject({
  id: v.string(),
})

export function createStandardPageSchema<T extends v.GenericSchema>(itemSchema: T) {
  return v.looseObject({
    content: v.array(itemSchema),
  })
}
export function createInfinitePageSchema<T extends v.GenericSchema>(itemSchema: T) {
  return v.looseObject({
    pages: v.array(itemSchema),
  })
}

const StandardPageSchema = createStandardPageSchema(HasIdSchema)
const InfinitePageSchema = createInfinitePageSchema(StandardPageSchema)

/**
 * Generate a predicate function that can be passed to `queryCache.invalidateQueries`.
 * @param target the value to match against the {@link key}
 * @param key the key in the data object to match against {@link target}. Defaults to `id`.
 */
export const expireCachePredicate =
  (
    target: string,
    key: string = 'id',
  ): ((entry: UseQueryEntry<unknown, unknown, unknown>) => boolean) =>
  (entry) => {
    const data = entry.state.value.data

    if (v.is(HasIdSchema, data)) return data[key] === target
    if (v.is(StandardPageSchema, data)) return data.content.some((item) => item[key] === target)
    if (v.is(InfinitePageSchema, data)) {
      return data.pages.some((page) => page.content.some((item) => item[key] === target))
    }
    return false
  }

function entityChanged(
  key: EntryKey,
  targetId: string,
  predicate: ReturnType<typeof expireCachePredicate> = expireCachePredicate(targetId),
) {
  const queryCache = useQueryCache()

  void queryCache.invalidateQueries({
    key: key,
    predicate: predicate,
  })
}

function entitiesChanged(key: EntryKey) {
  const queryCache = useQueryCache()

  void queryCache.invalidateQueries({ key: key })
}

/**
 * Clears all the caches.
 */
export function clearAll() {
  const queryCache = useQueryCache()

  // cancel everything in-flight
  queryCache.cancelQueries()

  // drop all entries from the cache
  queryCache.getEntries().forEach((entry) => queryCache.remove(entry))
}

export function clearBook(bookId: string) {
  void entityChanged(QUERY_KEYS_BOOKS.root, bookId)
}

export function clearAllBooks() {
  void entitiesChanged(QUERY_KEYS_BOOKS.root)
}

export function clearSeries(seriesId: string, clearChildrenBooks: boolean) {
  void entityChanged(QUERY_KEYS_SERIES.root, seriesId)
  if (clearChildrenBooks)
    void entityChanged(QUERY_KEYS_BOOKS.root, seriesId, expireCachePredicate(seriesId, 'seriesId'))
}

export function clearAllSeries() {
  void entitiesChanged(QUERY_KEYS_SERIES.root)
}

export function clearAllLibraries() {
  void entitiesChanged(QUERY_KEYS_LIBRARIES.root)
}

export function clearAllReadLists() {
  void entitiesChanged(QUERY_KEYS_READLIST.root)
}

export function clearReadList(readListId: string) {
  void entityChanged(QUERY_KEYS_READLIST.root, readListId)
  void entitiesChanged(QUERY_KEYS_READLIST.byBook())
}

export function clearAllCollections() {
  void entitiesChanged(QUERY_KEYS_READLIST.root)
}

export function clearCollection(collectionId: string) {
  void entityChanged(QUERY_KEYS_COLLECTIONS.root, collectionId)
  void entitiesChanged(QUERY_KEYS_COLLECTIONS.bySeries())
}

export function clearThumbnailBook(bookId: string) {
  void entitiesChanged(QUERY_KEYS_BOOKS.posters(bookId))
}

export function clearThumbnailSeries(seriesId: string) {
  void entitiesChanged(QUERY_KEYS_SERIES.posters(seriesId))
}

export function clearThumbnailReadList(readListId: string) {
  void entitiesChanged(QUERY_KEYS_READLIST.posters(readListId))
}

export function clearThumbnailCollection(collectionId: string) {
  void entitiesChanged(QUERY_KEYS_COLLECTIONS.posters(collectionId))
}
