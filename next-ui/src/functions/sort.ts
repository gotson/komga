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
