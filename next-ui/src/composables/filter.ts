import * as v from 'valibot'
import {
  type FilterContext,
  filterKeys,
  SchemaFilterContributors,
  SchemaFilterContributorsRecord,
  SchemaFilterMediaProfile,
  SchemaFilterMediaStatus,
  SchemaFilterNotableEntities,
  SchemaFilterNotableEntitiesRecord,
  SchemaFilterPoster,
  SchemaFilterReadStatus,
  SchemaFilterSeriesStatus,
  SchemaFilterStrings,
  SchemaIncludeExclude,
  SchemaSeriesAgeRatings,
  SchemaSeriesReleaseYears,
} from '@/types/filter'
import { useRouteQuerySchema } from '@/composables/useRouteQuerySchema'
import { CONTRIBUTOR_ANYROLE, contributorsRolesMessages } from '@/types/referential/contributor'
import { useQuery } from '@pinia/colada'
import { authorsRolesQuery, notableEntitiesKindsQuery } from '@/colada/referential'
import { PageRequest } from '@/types/PageRequest'
import { watchImmediate } from '@vueuse/core'
import { createOrderCompareFn } from '@/functions/sort'
import { clearFilter, countFilter } from '@/functions/filter'
import type { MaybeRefOrGetter, UnwrapRef } from 'vue'
import { deepEqual } from 'fast-equals'
import {
  NOTABLE_ENTITY_ANYKIND,
  notableEntitiesKindsMessages,
} from '@/types/referential/notableEntity'

export const QueryParamContributors = 'contributors'
export const QueryParamNotableEntities = 'entities'

function useFilterContributors(context?: MaybeRefOrGetter<FilterContext>) {
  // the update function for the query param
  function updateRouteFn(data: v.InferOutput<typeof SchemaFilterContributorsRecord>) {
    const defaults = v.getDefaults(SchemaFilterContributors)
    const notDefault: v.InferOutput<typeof SchemaFilterContributorsRecord> = {}
    Object.entries(data).forEach(([role, value]) => {
      if (!deepEqual(value, defaults)) notDefault[role] = value
    })
    if (Object.keys(notDefault).length > 0) return JSON.stringify(notDefault)
    return undefined
  }

  const filterContributors = useRouteQuerySchema(
    QueryParamContributors,
    SchemaFilterContributorsRecord,
    updateRouteFn,
  ).data

  // get the roles dynamically from the filter context
  const filterContext = inject(filterKeys.context, context ?? {})
  const { data } = useQuery(() =>
    authorsRolesQuery({
      ...toValue(filterContext),
      pageRequest: PageRequest.Unpaged(),
    }),
  )

  watchImmediate(data, (newData) => {
    if (!newData || !newData.content) return

    // server roles + the special role for any role
    const serverRoles = [
      CONTRIBUTOR_ANYROLE,
      ...newData.content.toSorted(
        createOrderCompareFn(Object.keys(contributorsRolesMessages), (role) => role),
      ),
    ]
    // delete extra roles that are not returned by the API
    Object.keys(filterContributors.value).forEach((role) => {
      if (!serverRoles.includes(role)) delete filterContributors.value[role]
    })

    // add default empty filter for roles not yet in the filter
    serverRoles.forEach((role) => {
      if (!filterContributors.value[role]) filterContributors.value[role] = { m: 'anyOf', v: [] }
    })
  })

  const count = computed(() =>
    Object.values(filterContributors.value)
      .map((filter) => filter.v.length)
      .reduce((sum, item) => sum + item, 0),
  )

  function clear() {
    Object.values(filterContributors.value).forEach((it) => clearFilter(it))
  }

  function clearOne(role: string) {
    if (filterContributors.value[role]) clearFilter(filterContributors.value[role])
  }

  return {
    filter: filterContributors,
    count,
    clear,
    clearOne,
  }
}

function useFilterNotableEntities(context?: MaybeRefOrGetter<FilterContext>) {
  // the update function for the query param
  function updateRouteFn(data: v.InferOutput<typeof SchemaFilterNotableEntitiesRecord>) {
    const defaults = v.getDefaults(SchemaFilterNotableEntities)
    const notDefault: v.InferOutput<typeof SchemaFilterNotableEntitiesRecord> = {}
    Object.entries(data).forEach(([kind, value]) => {
      if (!deepEqual(value, defaults)) notDefault[kind] = value
    })
    if (Object.keys(notDefault).length > 0) return JSON.stringify(notDefault)
    return undefined
  }

  const filterNotableEntities = useRouteQuerySchema(
    QueryParamNotableEntities,
    SchemaFilterNotableEntitiesRecord,
    updateRouteFn,
  ).data

  // get the kinds dynamically from the filter context
  const filterContext = inject(filterKeys.context, context ?? {})
  const { data } = useQuery(() =>
    notableEntitiesKindsQuery({
      ...toValue(filterContext),
      pageRequest: PageRequest.Unpaged(),
    }),
  )

  watchImmediate(data, (newData) => {
    if (!newData || !newData.content) return

    // server kinds + the special kind for any kind
    const serverKinds = [
      NOTABLE_ENTITY_ANYKIND,
      ...newData.content.toSorted(
        createOrderCompareFn(Object.keys(notableEntitiesKindsMessages), (kind) => kind),
      ),
    ]
    // delete extra kinds that are not returned by the API
    Object.keys(filterNotableEntities.value).forEach((kind) => {
      if (!serverKinds.includes(kind)) delete filterNotableEntities.value[kind]
    })

    // add default empty filter for kinds not yet in the filter
    serverKinds.forEach((kind) => {
      if (!filterNotableEntities.value[kind])
        filterNotableEntities.value[kind] = { m: 'anyOf', v: [] }
    })
  })

  const count = computed(() =>
    Object.values(filterNotableEntities.value)
      .map((filter) => filter.v.length)
      .reduce((sum, item) => sum + item, 0),
  )

  function clear() {
    Object.values(filterNotableEntities.value).forEach((it) => clearFilter(it))
  }

  function clearOne(kind: string) {
    if (filterNotableEntities.value[kind]) clearFilter(filterNotableEntities.value[kind])
  }

  return {
    filter: filterNotableEntities,
    count,
    clear,
    clearOne,
  }
}

// list of supported standard filters and their associated schema
const standardFilters = {
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
  poster: SchemaFilterPoster,
}

export type StandardFilters = {
  [K in keyof typeof standardFilters]: v.InferOutput<(typeof standardFilters)[K]>
}

type StandardFilterValue<T extends v.GenericSchema> = {
  filter: Ref<v.InferOutput<T>>
  count: ComputedRef<number>
  clear: () => void
}

type SupportedFilterKeys = keyof typeof standardFilters | 'contributors' | 'notableEntities'

type SupportedFiltersOutput = {
  [K in SupportedFilterKeys]: K extends keyof typeof standardFilters
    ? StandardFilterValue<(typeof standardFilters)[K]>
    : K extends 'contributors'
      ? ReturnType<typeof useFilterContributors>
      : K extends 'notableEntities'
        ? ReturnType<typeof useFilterNotableEntities>
        : never
}

function isStandardFilterKey(key: SupportedFilterKeys): key is keyof typeof standardFilters {
  return key in standardFilters
}

export function useFilters<K extends SupportedFilterKeys>(
  filterTypes: K[],
  context?: MaybeRefOrGetter<FilterContext>,
) {
  type SelectedFilter = Pick<SupportedFiltersOutput, K>
  const filters = ref<SelectedFilter>({} as SelectedFilter)

  filterTypes.forEach((type) => {
    if (type === 'contributors') {
      filters.value[type] = useFilterContributors(context)
    } else if (type === 'notableEntities') {
      filters.value[type] = useFilterNotableEntities(context)
    } else if (isStandardFilterKey(type)) {
      const filter = useRouteQuerySchema(type, standardFilters[type]).data
      filters.value[type] = {
        filter: filter,
        count: computed(() => countFilter(filter.value)),
        clear: () => clearFilter(filter.value),
      }
    }
  })

  function clearAll() {
    Object.values(filters.value).forEach((it) => (it as StandardFilterValue<never>).clear())
  }

  const countAll = computed(() =>
    Object.values(filters.value)
      .map((it) => (it as UnwrapRef<StandardFilterValue<never>>).count)
      .reduce((sum, val) => sum + val, 0),
  )

  return {
    filters,
    clearAll,
    countAll,
  }
}
