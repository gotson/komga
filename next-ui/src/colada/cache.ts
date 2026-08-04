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

function expireCachePredicate(
  targetId: string,
): (entry: UseQueryEntry<unknown, unknown, unknown>) => boolean {
  return (entry) => {
    const data = entry.state.value.data

    if (v.is(HasIdSchema, data)) return data.id === targetId
    if (v.is(StandardPageSchema, data)) return data.content.some((item) => item.id === targetId)
    if (v.is(InfinitePageSchema, data)) {
      return data.pages.some((page) => page.content.some((item) => item.id === targetId))
    }
    return false
  }
}

export function entityChanged(key: EntryKey, targetId: string) {
  const queryCache = useQueryCache()

  void queryCache.invalidateQueries({
    key: key,
    predicate: expireCachePredicate(targetId),
  })
}

export function entitiesChanged(key: EntryKey) {
  const queryCache = useQueryCache()

  void queryCache.invalidateQueries({ key: key })
}

/**
 * Clears all the caches.
 */
export function invalidateAll() {
  const queryCache = useQueryCache()
  void queryCache.invalidateQueries()
}
