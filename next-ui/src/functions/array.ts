import { deepEqual } from 'fast-equals'

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

/**
 * Compares two arrays for set equality (ignoring order).
 * Supports comparing arrays of different types (e.g., T[] vs U[]) using key selectors.
 */
export function isSameSet<T, U = T, K = unknown>(
  arrA: readonly T[],
  arrB: readonly U[],
  keySelectorA?: (item: T) => K,
  keySelectorB?: (item: U) => K,
): boolean {
  if (arrA.length !== arrB.length) return false

  const selectA = keySelectorA ?? ((item) => item as unknown as K)
  const selectB = keySelectorB ?? ((item) => item as unknown as K)

  const setA = new Set(arrA.map(selectA))
  const setB = new Set(arrB.map(selectB))

  if (setA.size !== setB.size) return false

  return setA.values().every((key) => setB.has(key))
}

/**
 * Count occurrences of elements across multiple arrays.
 * @param arrays
 */
export function countAllOccurrences<T>(...arrays: T[][]): Map<T, number> {
  const entries: { item: T; count: number }[] = []

  for (const array of arrays) {
    for (const item of array) {
      const entry = entries.find((e) => deepEqual(e.item, item))

      if (entry) {
        entry.count += 1
      } else {
        entries.push({ item, count: 1 })
      }
    }
  }

  const counts = new Map<T, number>()
  for (const { item, count } of entries) {
    counts.set(item, count)
  }

  return counts
}

/**
 * Extracts element types from either `T[]` or `T`.
 */
export type UnwrappedElement<T> = T extends (infer U)[]
  ? UnwrappedElement<U>
  : T extends undefined
    ? null
    : T extends null
      ? null
      : Exclude<T, undefined> | (undefined extends T ? null : never)

/**
 * Helper to flatten single values or arrays into a single flat array.
 */
export function extractFlatValues<E, K extends keyof E>(
  entities: E[],
  key: K,
): (UnwrappedElement<E[K]> | null)[] {
  type ItemType = UnwrappedElement<E[K]> | null
  const result: ItemType[] = []

  for (const entity of entities) {
    const val = entity[key]
    if (val === undefined || val === null) result.push(null)
    else if (Array.isArray(val)) {
      for (const item of val) {
        if (item !== undefined && item !== null) {
          result.push(item as ItemType)
        }
      }
    } else {
      result.push(val as ItemType)
    }
  }

  return result
}

/**
 * Check whether boolean values in an array are all set or not.
 * @param elements
 */
export function getBooleanArrayState(elements: boolean[]): 'all' | 'none' | 'some' {
  if (elements.length === 0) return 'none'

  const hasTrue = elements.some((it) => it)
  const hasFalse = elements.some((it) => !it)

  if (hasTrue && !hasFalse) return 'all'
  if (!hasTrue) return 'none'
  return 'some'
}
