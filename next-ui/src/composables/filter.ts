import * as v from 'valibot'
import { filterKeys, SchemaFilterCreators, SchemaFilterCreatorsRecord } from '@/types/filter'
import { useRouteQuerySchema } from '@/composables/useRouteQuerySchema'
import { creatorRolesMessages } from '@/types/referential'
import { useQuery } from '@pinia/colada'
import { authorsRolesQuery } from '@/colada/referential'
import { PageRequest } from '@/types/PageRequest'
import { watchImmediate } from '@vueuse/core'
import { createOrderCompareFn } from '@/functions/sort'
import { clearFilter } from '@/functions/filter'

export function useFilterCreators() {
  // the update function for the query param
  function updateRouteFn(data: v.InferOutput<typeof SchemaFilterCreatorsRecord>) {
    const defaults = v.getDefaults(SchemaFilterCreators)
    const notDefault: v.InferOutput<typeof SchemaFilterCreatorsRecord> = {}
    Object.entries(data).forEach(([role, value]) => {
      if (JSON.stringify(value) !== JSON.stringify(defaults)) notDefault[role] = value
    })
    if (Object.keys(notDefault).length > 0) return JSON.stringify(notDefault)
    return undefined
  }

  const filterCreators = useRouteQuerySchema(
    'creators',
    SchemaFilterCreatorsRecord,
    updateRouteFn,
  ).data
  // always add the special role for any role
  filterCreators.value['anyrole'] = { m: 'anyOf', v: [] }

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
      .toSorted(createOrderCompareFn(Object.keys(creatorRolesMessages), (role) => role))
      .forEach((role) => {
        filterCreators.value[role] = { m: 'anyOf', v: [] }
      })
  })

  const countAll = computed(() =>
    Object.values(filterCreators.value)
      .map((filter) => filter.v.length)
      .reduce((sum, item) => sum + item, 0),
  )

  function clearAll() {
    Object.values(filterCreators.value).forEach((it) => clearFilter(it))
  }

  function clear(role: string) {
    if (filterCreators.value[role]) clearFilter(filterCreators.value[role])
  }

  return {
    filter: filterCreators,
    countAll,
    clearAll,
    clear,
  }
}
