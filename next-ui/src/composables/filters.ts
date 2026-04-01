import * as v from 'valibot'
import { SchemaFilterAuthors } from '@/types/filter'
import { useRouteQuerySchema } from '@/composables/useRouteQuerySchema'
import { authorRoles } from '@/types/referential'
import { useIntl } from 'vue-intl'

export function useFilterAuthors() {
  const intl = useIntl()

  const filterAuthors = reactive<
    Record<
      string,
      { filter: v.InferOutput<typeof SchemaFilterAuthors>; text: string; role?: string }
    >
  >({})

  filterAuthors['anyrole'] = {
    filter: useRouteQuerySchema('anyrole', SchemaFilterAuthors).data.value,
    text: intl.formatMessage({
      description: 'Author filter: any role',
      defaultMessage: 'All creators',
      id: 'RmNasP',
    }),
  }
  // TODO: get roles dynamically
  Object.entries(authorRoles).forEach(([role, value]) => {
    filterAuthors[role] = {
      filter: useRouteQuerySchema(role, SchemaFilterAuthors).data.value,
      text: intl.formatMessage(value),
      role: role,
    }
  })

  return {
    filterAuthors: filterAuthors,
  }
}
