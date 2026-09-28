import type { Sort } from '@/types/PageRequest'
import * as v from 'valibot'
import type { SortOption } from '@/types/sort'

/**
 * Creates a compare function for Array.prototype.sort()
 * that orders items based on a predefined array of keys.
 *
 * @param orderKeys The array defining the exact sort order.
 * @param keySelector A function to extract the string key from the item being sorted.
 * @param orderSelector A function to extract the string key from the order array. Defaults to String(it).
 * @returns A compare function to be passed directly to .sort()
 */
export function createOrderCompareFn<K, T>(
  orderKeys: K[],
  keySelector: (item: T) => string,
  orderSelector: (item: K) => string = (it) => String(it),
): (a: T, b: T) => number {
  // Create the map once when the factory is called
  const orderMap = new Map(orderKeys.map((key, index) => [orderSelector(key), index]))

  // Return the actual comparison function expected by .sort()
  return (a: T, b: T) => {
    const indexA = orderMap.get(keySelector(a)) ?? Infinity
    const indexB = orderMap.get(keySelector(b)) ?? Infinity

    return indexA - indexB
  }
}

const SortItemSchema = v.pipe(
  v.string(),
  // Regex ensures it's either "key", "key,asc", or "key,desc"
  v.regex(/^[a-zA-Z0-9._]+(,(asc|desc))?$/i, 'Invalid sort format'),
  v.transform((input): Sort => {
    // The regex guarantees the split will yield a valid key
    const [key = '', order] = input.split(',')

    return {
      key,
      order: order === 'desc' ? 'desc' : 'asc',
    }
  }),
)

export const MultiSortSchema = v.optional(v.array(SortItemSchema), [])

/**
 * Converts a Sort object to a string format (e.g., "title,asc")
 */
export function stringifySort(sort: Sort): string {
  // Defaults to 'asc' if order is omitted
  return `${sort.key},${sort.order || 'asc'}`
}

export function sortOptionToSorts(option: SortOption): Sort[] {
  const sorts: Sort[] = [
    {
      key: option.key,
      order: option.initialOrder,
    },
  ]
  if (option.invertible)
    sorts.push({
      key: option.key,
      order: option.initialOrder === 'asc' ? 'desc' : 'asc',
    })
  return sorts
}
