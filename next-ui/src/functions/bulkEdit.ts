import type {
  BulkInputProps,
  BulkItemMultiField,
  BulkItemSingleField,
  InitialCountItem,
} from '@/types/bulk'
import { countAllOccurrences, extractFlatValues, type UnwrappedElement } from '@/functions/array'
import type { NormalizeNullable } from '@/types/entityUpdate'

export function initializeBulkEdit<T>(
  props: BulkInputProps<T>,
  defaultValue: null extends T ? null : T,
) {
  let initialValue: BulkItemSingleField<T> = { state: 'mixed' }

  if (props.initialCounts.length === 0)
    initialValue = {
      state: 'all',
      value: defaultValue,
    }

  if (props.initialCounts.length > 0) {
    for (const rawItem of props.initialCounts) {
      const count = rawItem.count

      // select the first element that has a count matching the totalCount
      if (count >= props.totalCount) {
        initialValue = { value: rawItem.value, state: 'all' }
      }
    }
  }

  return {
    initialValue: initialValue,
    isMixed: initialValue.state === 'mixed',
  }
}

function toInitialCounts<T>(map: Map<T, number>): InitialCountItem<T>[] {
  return Array.from(map.entries()).map(([value, count]) => ({ value: value, count: count }))
}

/**
 * Computes initial counts for a list of property keys on an array of entities.
 */
export function getInitialCountsForKeys<
  E extends Record<string, unknown>,
  K extends keyof E & string,
>(
  entities: E[],
  keys: K[],
): {
  // If E[P] or its array elements allow null/undefined, return InitialCountItem<UnwrappedElement<E[P]>>
  // (which already includes null). Otherwise, type it as UnwrappedElement<E[P]> | null to account for empty data.
  [P in K]: InitialCountItem<UnwrappedElement<E[P]>>[]
} {
  type ReturnType = {
    [P in K]: InitialCountItem<UnwrappedElement<E[P]>>[]
  }

  const result = {} as ReturnType

  for (const key of keys) {
    const values = extractFlatValues(entities, key)
    const map = countAllOccurrences(values)
    result[key] = toInitialCounts(map) as ReturnType[K]
  }

  return result
}

/**
 * Applies a single-field bulk update if state is 'all'
 */
function applyBulkSingleField<T>(field: BulkItemSingleField<T> | undefined, currentValue: T): T {
  if (field && field.state === 'all') {
    return field.value
  }
  return currentValue
}

/**
 * Applies multi-field bulk updates (handles 'remove' and 'new')
 */
function applyBulkMultiField<T>(
  items: BulkItemMultiField<T>[] | undefined,
  currentArray: T[] = [],
): T[] {
  if (!items) return currentArray

  let result = [...currentArray]

  for (const item of items) {
    if (item.state === 'remove') {
      result = result.filter((it) => it !== item.value)
    } else if (item.state === 'new' && !result.includes(item.value)) {
      result.push(item.value)
    }
  }

  return result
}

/**
 * Type-safe bulk update mapper.
 * Derives allowable bulk updates based on the target DTO properties.
 */
export function applyBulkUpdates<
  TDto extends Record<string, unknown>,
  TBulk extends Partial<{
    [K in keyof TDto]: NonNullable<TDto[K]> extends (infer E)[]
      ? BulkItemMultiField<E>[]
      : BulkItemSingleField<NormalizeNullable<TDto[K]>>
  }>,
>(targetDto: TDto, bulkUpdates: TBulk): TDto {
  const result = { ...targetDto }

  for (const key of Object.keys(bulkUpdates) as Array<keyof TBulk & keyof TDto>) {
    const updateValue = bulkUpdates[key]
    if (updateValue === undefined) continue

    const currentTargetValue = result[key]

    if (Array.isArray(currentTargetValue) && Array.isArray(updateValue)) {
      result[key] = applyBulkMultiField(updateValue, currentTargetValue) as TDto[typeof key]
    } else {
      result[key] = applyBulkSingleField(
        updateValue as BulkItemSingleField<NormalizeNullable<TDto[keyof TDto]>>,
        currentTargetValue as NormalizeNullable<TDto[keyof TDto]>,
      ) as TDto[typeof key]
    }
  }

  return result
}
