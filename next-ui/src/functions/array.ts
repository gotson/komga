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
