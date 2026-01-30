import * as v from 'valibot'
import { useRouteQuery } from '@vueuse/router'
import { syncRef } from '@vueuse/core'

/**
 * Reactive `route.query` with schema validation.
 * If value is not valid, the `schema` default values are used.
 *
 * @param queryName the query parameter name
 * @param schema valibot schema to validate against
 */
export function useRouteQuerySchema<T extends v.GenericSchema>(queryName: string, schema: T) {
  const queryString = useRouteQuery(queryName, '{}')

  const defaults = v.getDefaults(schema)

  function getInitialValue(stringValue: string) {
    try {
      return v.parse(schema, JSON.parse(stringValue))
    } catch {
      return defaults
    }
  }

  const data = ref(getInitialValue(String(queryString.value)))

  syncRef(data, queryString, {
    direction: 'ltr',
    deep: true,
    transform: {
      ltr: (left) => {
        if (JSON.stringify(left) !== JSON.stringify(defaults)) return JSON.stringify(left)
        return undefined
      },
    },
  })

  return {
    data: data as v.InferOutput<T>,
  }
}
