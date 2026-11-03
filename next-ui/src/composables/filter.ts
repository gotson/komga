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
import { watchDeep, watchImmediate } from '@vueuse/core'
import { createOrderCompareFn } from '@/functions/sort'
import { clearFilter, countFilter } from '@/functions/filter'
import type { MaybeRefOrGetter, UnwrapRef } from 'vue'
import { deepEqual } from 'fast-equals'
import {
  NOTABLE_ENTITY_ANYKIND,
  notableEntitiesKindsMessages,
} from '@/types/referential/notableEntity'
import { useFilterStore } from '@/stores/filter'
import { logger } from '@/services/logtape'

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

  const isReady = ref(false)
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

    isReady.value = true
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
    isReady,
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

  const isReady = ref(false)
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

    isReady.value = true
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
    isReady,
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

export type SupportedFilterKeys = keyof typeof standardFilters | 'contributors' | 'notableEntities'

export type SupportedFiltersOutput = {
  [K in SupportedFilterKeys]: K extends keyof typeof standardFilters
    ? StandardFilterValue<(typeof standardFilters)[K]>
    : K extends 'contributors'
      ? ReturnType<typeof useFilterContributors>
      : K extends 'notableEntities'
        ? ReturnType<typeof useFilterNotableEntities>
        : never
}

export type FilterStateMap = {
  [K in SupportedFilterKeys]: UnwrapRef<SupportedFiltersOutput[K]>['filter']
}

function isStandardFilterKey(key: SupportedFilterKeys): key is keyof typeof standardFilters {
  return key in standardFilters
}

type FiltersConfig = {
  context?: MaybeRefOrGetter<FilterContext>
  persist?: boolean
  viewName?: string
}

export function useFilters<K extends SupportedFilterKeys>(
  filterTypes: K[],
  config: FiltersConfig = {},
) {
  const { context, persist, viewName } = config

  const filterStore = useFilterStore()

  type SelectedFilter = Pick<SupportedFiltersOutput, K>
  const filters = ref<SelectedFilter>({} as SelectedFilter)

  // store the readiness of each filter, in case they depend on async data
  const readyRefs: Ref<boolean>[] = []

  const isPersistMode = ref(false)

  const isReady = computed(() => readyRefs.every((r) => r.value))

  const countAll = computed(() =>
    Object.values(filters.value)
      .map((it) => (it as UnwrapRef<StandardFilterValue<never>>).count)
      .reduce((sum, val) => sum + val, 0),
  )

  // build filters
  filterTypes.forEach((type) => {
    if (type === 'contributors') {
      const filterContributors = useFilterContributors(context)
      readyRefs.push(filterContributors.isReady)
      filters.value[type] = filterContributors
    } else if (type === 'notableEntities') {
      const filterNotableEntities = useFilterNotableEntities(context)
      readyRefs.push(filterNotableEntities.isReady)
      filters.value[type] = filterNotableEntities
    } else if (isStandardFilterKey(type)) {
      const filter = useRouteQuerySchema(type, standardFilters[type]).data
      readyRefs.push(ref(true))
      filters.value[type] = {
        filter: filter,
        count: computed(() => countFilter(filter.value)),
        clear: () => clearFilter(filter.value),
      }
    }
  })

  // persistence handling
  if (persist && viewName) {
    let unwatchIsReady: (() => void) | undefined = undefined
    unwatchIsReady = watchImmediate(isReady, (ready) => {
      if (ready) {
        const isFilterSameAsPersisted = deepEqual(
          getSnapshot(),
          filterStore.getSavedFilters(viewName),
        )
        logger.debug(`query filter equals stored filter: ${isFilterSameAsPersisted}`)

        const isFilterClean = countAll.value === 0
        logger.debug(`query filter is clean: ${isFilterClean}`)

        isPersistMode.value = isFilterClean || isFilterSameAsPersisted
        if (isPersistMode.value) {
          restoreFromStore()

          watchDeep(
            () => getSnapshot(),
            () => {
              if (isPersistMode.value) persistToStore()
            },
          )
        }

        unwatchIsReady?.()
      }
    })
  }

  function clearAll() {
    Object.values(filters.value).forEach((it) => (it as StandardFilterValue<never>).clear())
  }

  function getSnapshot(): Partial<FilterStateMap> {
    const state: Partial<FilterStateMap> = {}
    Object.entries(filters.value).forEach(([key, val]) => {
      const targetFilter = (val as { filter: unknown }).filter
      const rawValue =
        typeof targetFilter === 'object' && targetFilter !== null && 'value' in targetFilter
          ? (targetFilter as Ref<unknown>).value
          : targetFilter

      state[key as K] = JSON.parse(JSON.stringify(rawValue))
    })
    return state
  }

  function restore(state: Partial<FilterStateMap>) {
    Object.entries(state).forEach(([key, value]) => {
      const target = filters.value[key as K]
      if (!target || value === undefined) return

      ;(target as { filter: unknown }).filter = JSON.parse(JSON.stringify(value))
    })
  }

  function persistToStore() {
    if (viewName) {
      filterStore.saveFilterState(viewName, getSnapshot())
    }
  }

  function restoreFromStore() {
    if (viewName) {
      const savedFilters = filterStore.getSavedFilters(viewName)
      if (savedFilters) {
        restore(savedFilters)
      }
    }
  }

  return {
    filters,
    clearAll,
    countAll,
    persistToStore,
    restoreFromStore,
    isPersistMode,
  }
}
