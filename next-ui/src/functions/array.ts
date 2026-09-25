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

export function getSurroundingElements<T>(
  array: T[],
  index: number,
  beforeCount: number = 0,
  afterCount: number = 0,
): T[] {
  if (index < 0 || index >= array.length) return []

  // slice before
  const startBefore = Math.max(0, index - beforeCount)
  const before = array.slice(startBefore, index)

  // slice after
  const endAfter = Math.min(array.length, index + 1 + afterCount)
  const after = array.slice(index + 1, endAfter)

  return [...before, ...after]
}

/**
 * Finds the element directly before or after a target element in an array based on a predicate.
 *
 * @param array - The source array to search.
 * @param predicate - A callback function to locate the target element.
 * @param position - Specifies whether to find the element 'before' or 'after'.
 * @returns The adjacent element, or undefined if not found or out of bounds.
 */
export function findAdjacent<T>(
  array: T[] | undefined,
  predicate: (element: T, index: number, array: T[]) => boolean,
  position: 'before' | 'after',
): T | undefined {
  if (!array) return undefined

  const targetIndex = array.findIndex(predicate)

  if (targetIndex === -1) {
    return undefined // Target element not found
  }

  const adjacentIndex = position === 'before' ? targetIndex - 1 : targetIndex + 1

  // Ensure adjacentIndex is within array boundaries
  if (adjacentIndex < 0 || adjacentIndex >= array.length) {
    return undefined
  }

  return array[adjacentIndex]
}
