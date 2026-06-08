/**
 * Creates a compare function for Array.prototype.sort()
 * that orders items based on a predefined array of keys.
 *
 * @param orderKeys The array defining the exact sort order.
 * @param keySelector A function to extract the string key from the item being sorted.
 * @returns A compare function to be passed directly to .sort()
 */
export function createOrderCompareFn<T>(
  orderKeys: string[],
  keySelector: (item: T) => string,
): (a: T, b: T) => number {
  // Create the map once when the factory is called
  const orderMap = new Map(orderKeys.map((key, index) => [key, index]))

  // Return the actual comparison function expected by .sort()
  return (a: T, b: T) => {
    const indexA = orderMap.get(keySelector(a)) ?? Infinity
    const indexB = orderMap.get(keySelector(b)) ?? Infinity

    return indexA - indexB
  }
}
