import * as v from 'valibot'
import { filterKeys, SchemaFilterContributors, SchemaFilterContributorsRecord } from '@/types/filter'
import { useRouteQuerySchema } from '@/composables/useRouteQuerySchema'
import { contributorsRolesMessages } from '@/types/referential'
import { useQuery } from '@pinia/colada'
import { authorsRolesQuery } from '@/colada/referential'
import { PageRequest } from '@/types/PageRequest'
import { watchImmediate } from '@vueuse/core'
import { createOrderCompareFn } from '@/functions/sort'
import { clearFilter } from '@/functions/filter'

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
