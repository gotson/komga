import { useRouteQuerySchema } from '@/composables/useRouteQuerySchema'
import { type Sort } from '@/types/PageRequest'
import { MultiSortSchema, sortDefinitionToSorts, stringifySort } from '@/functions/sort'
import { deepEqual } from 'fast-equals'
import type { SortOption } from '@/types/sort'
import { useSortStore } from '@/stores/sort'
import { logger } from '@/services/logtape'

export const QueryParamSort = 'sort'

type SortConfig = {
  defaultSort: Sort[]
  options: SortOption[]
  multiSort?: boolean
  viewName?: string
  persist?: boolean
}

export function useSort(config: SortConfig) {
  const { defaultSort, options, multiSort, persist, viewName } = config

  const sortStore = useSortStore()

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

  const isDefault = computed(() => deepEqual(sort.value, defaultSort))

  if (persist && viewName) {
    const savedSorts = sortStore.getSavedSorts(viewName)

    // restore saved state only if the current state is default
    if (isDefault.value && savedSorts && savedSorts.length > 0) {
      logger.debug('restore sort from saved state')
      sort.value = JSON.parse(JSON.stringify(savedSorts))
    }
  }

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
          else restoreDefault()
        }

        if (persist && viewName) {
          sortStore.saveSortsState(viewName, JSON.parse(JSON.stringify(val)))
        }
      }
    },
    {
      immediate: true,
      deep: true,
    },
  )

  function restoreDefault() {
    sort.value = structuredClone(defaultSort)
  }

  function persistToStore() {
    if (viewName) {
      sortStore.saveSortsState(viewName, JSON.parse(JSON.stringify(sort.value)))
    }
  }

  function restoreFromStore() {
    if (viewName) {
      const savedSorts = sortStore.getSavedSorts(viewName)
      if (savedSorts) {
        sort.value = savedSorts
      }
    }
  }

  return {
    sortActive: sort,
    restoreDefault,
    persistToStore,
    restoreFromStore,
    isDefault,
  }
}
