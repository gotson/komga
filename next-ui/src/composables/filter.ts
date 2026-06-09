import * as v from 'valibot'
import {
  filterKeys,
  SchemaFilterContributors,
  SchemaFilterContributorsRecord,
  SchemaFilterMediaProfile,
  SchemaFilterMediaStatus,
  SchemaFilterReadStatus,
  SchemaFilterSeriesStatus,
  SchemaFilterStrings,
  SchemaIncludeExclude,
  SchemaSeriesAgeRatings,
  SchemaSeriesReleaseYears,
} from '@/types/filter'
import { useRouteQuerySchema } from '@/composables/useRouteQuerySchema'
import { contributorsRolesMessages } from '@/types/referential'
import { useQuery } from '@pinia/colada'
import { authorsRolesQuery } from '@/colada/referential'
import { PageRequest } from '@/types/PageRequest'
import { watchImmediate } from '@vueuse/core'
import { createOrderCompareFn } from '@/functions/sort'
import { clearFilter, countFilter } from '@/functions/filter'
import type { UnwrapRef } from 'vue'

export function useFilterContributors() {
  // the update function for the query param
  function updateRouteFn(data: v.InferOutput<typeof SchemaFilterContributorsRecord>) {
    const defaults = v.getDefaults(SchemaFilterContributors)
    const notDefault: v.InferOutput<typeof SchemaFilterContributorsRecord> = {}
    Object.entries(data).forEach(([role, value]) => {
      if (JSON.stringify(value) !== JSON.stringify(defaults)) notDefault[role] = value
    })
    if (Object.keys(notDefault).length > 0) return JSON.stringify(notDefault)
    return undefined
  }

  const filterContributors = useRouteQuerySchema(
    'contributors',
    SchemaFilterContributorsRecord,
    updateRouteFn,
  ).data
  // always add the special role for any role
  filterContributors.value['anyrole'] = { m: 'anyOf', v: [] }

  // get the roles dynamically from the filter context
  const filterContext = inject(filterKeys.context, {})
  const { data } = useQuery(() =>
    authorsRolesQuery({
      ...toValue(filterContext),
      pageRequest: PageRequest.Unpaged(),
    }),
  )

  watchImmediate(data, (newData) => {
    if (!newData || !newData.content) return

    newData.content
      .toSorted(createOrderCompareFn(Object.keys(contributorsRolesMessages), (role) => role))
      .forEach((role) => {
        filterContributors.value[role] = { m: 'anyOf', v: [] }
      })
  })

  const countAll = computed(() =>
    Object.values(filterContributors.value)
      .map((filter) => filter.v.length)
      .reduce((sum, item) => sum + item, 0),
  )

  function clearAll() {
    Object.values(filterContributors.value).forEach((it) => clearFilter(it))
  }

  function clear(role: string) {
    if (filterContributors.value[role]) clearFilter(filterContributors.value[role])
  }

  return {
    filter: filterContributors,
    countAll,
    clearAll,
    clear,
  }
}

// list of supported filters and their associated schema
const supportedFilters = {
  seriesStatus: SchemaFilterSeriesStatus,
  read: SchemaFilterReadStatus,
  genre: SchemaFilterStrings,
  library: SchemaFilterStrings,
  tag: SchemaFilterStrings,
  publisher: SchemaFilterStrings,
  sharingLabel: SchemaFilterStrings,
  language: SchemaFilterStrings,
  year: SchemaSeriesReleaseYears,
  age: SchemaSeriesAgeRatings,
  complete: SchemaIncludeExclude,
  unavailable: SchemaIncludeExclude,
  oneshot: SchemaIncludeExclude,
  mediaStatus: SchemaFilterMediaStatus,
  profile: SchemaFilterMediaProfile,
}
type SupportedFiltersOutput = {
  [K in keyof typeof supportedFilters]: FilterValue<(typeof supportedFilters)[K]>
}
type FilterValue<T extends v.GenericSchema> = {
  filter: Ref<v.InferOutput<T>>
  count: ComputedRef<number>
  clear: () => void
}

export function useFilters<K extends keyof typeof supportedFilters>(filterTypes: K[]) {
  type SelectedFilter = Pick<SupportedFiltersOutput, K>
  const filters = ref<SelectedFilter>({} as SelectedFilter)

  filterTypes.forEach((type) => {
    const filter = useRouteQuerySchema(type, supportedFilters[type]).data
    filters.value[type] = {
      filter: filter,
      count: computed(() => countFilter(filter.value)),
      clear: () => clearFilter(filter.value),
    }
  })

  function clearAll() {
    Object.values(filters.value).forEach((it) => (it as FilterValue<never>).clear())
  }

  const countAll = computed(() =>
    Object.values(filters.value)
      .map((it) => (it as UnwrapRef<FilterValue<never>>).count)
      .reduce((sum, val) => sum + val, 0),
  )

  return {
    filters,
    clearAll,
    countAll: countAll,
  }
}
