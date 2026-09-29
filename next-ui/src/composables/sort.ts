import { useRouteQuerySchema } from '@/composables/useRouteQuerySchema'
import { type Sort } from '@/types/PageRequest'
import { MultiSortSchema, sortDefinitionToSorts, stringifySort } from '@/functions/sort'
import { deepEqual } from 'fast-equals'
import type { SortOption } from '@/types/sort'

export const QueryParamSort = 'sort'

export function useSort(defaultSort: Sort[], options: SortOption[], multiSort: boolean) {
  const sort = useRouteQuerySchema(
    QueryParamSort,
    MultiSortSchema,
    (data) => {
      if (!data || deepEqual(data, defaultSort)) return undefined
      return JSON.stringify(data.map((it) => stringifySort(it)))
    },
    defaultSort,
  ).data

  const validSorts = options.flatMap((it) => sortDefinitionToSorts(it))

  // filter out invalid values
  watch(
    sort,
    (val) => {
      if (val) {
        const filtered = val.filter((it) => validSorts.some((valid) => deepEqual(valid, it)))
        const trimmed = multiSort ? filtered : filtered.slice(0, 1)

        // update only if needed
        if (!deepEqual(val, trimmed)) {
          if (filtered.length > 0) sort.value = trimmed
          else restore()
        }
      }
    },
    {
      immediate: true,
      deep: true,
    },
  )

  function restore() {
    sort.value = structuredClone(defaultSort)
  }

  const isDefault = computed(() => deepEqual(sort.value, defaultSort))

  return {
    sortActive: sort,
    restore,
    isDefault,
  }
}
