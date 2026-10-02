import * as v from 'valibot'
import { type EntryKey, useQueryCache, type UseQueryEntry } from '@pinia/colada'

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

export function entityChanged(
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

export function entitiesChanged(key: EntryKey) {
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
