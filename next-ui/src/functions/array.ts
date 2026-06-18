/**
 * Returns the intersection of multiple arrays.
 * Uses Generics <T> to preserve the type of the array elements.
 */
export function getIntersection<T>(...arrays: T[][]): T[] {
  // Return an empty array if no arguments are passed
  if (arrays.length === 0) return []

  return arrays.reduce((accumulator, currentArray) => {
    // Create a Set from the current array for fast O(1) lookups
    const currentSet = new Set(currentArray)

    // Keep only the items in the accumulator that also exist in the currentSet
    return accumulator.filter((item) => currentSet.has(item))
  })
}

export function pushIfAbsent<T>(
  destArray: unknown[],
  newElements: T[] | undefined,
  isSame: (existing: unknown, toAdd: T) => boolean = (existing, toAdd) =>
    JSON.stringify(existing) === JSON.stringify(toAdd),
) {
  if (!newElements) return
  newElements.forEach((newItem) => {
    if (!destArray.some((existingItem) => isSame(existingItem, newItem))) destArray.push(newItem)
  })
}
