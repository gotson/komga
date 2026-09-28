import * as v from 'valibot'
import { useRouteQuery } from '@vueuse/router'
import { syncRef } from '@vueuse/core'
import { deepEqual } from 'fast-equals'

/**
 * Reactive `route.query` with schema validation.
 * If value is not valid, the `schema` default values are used.
 *
 * @param queryName the query parameter name
 * @param schema valibot schema to validate against
 * @param updateQueryFn custom function to update the query param. The default function performs a deep equality comparison between value and the schema's defaults.
 * @param defaultValue default value to use if the query param cannot be parsed. The default is to use `v.getDefaults` on the {@link schema}
 */
export function useRouteQuerySchema<T extends v.GenericSchema>(
  queryName: string,
  schema: T,
  updateQueryFn?: (data: v.InferOutput<T>) => string | undefined,
  defaultValue?: v.InferOutput<T>,
) {
  const queryString = useRouteQuery(queryName, '{}')

  const defaults =
    defaultValue !== undefined ? structuredClone(defaultValue) : v.getDefaults(schema)

  function getInitialValue(stringValue: string) {
    try {
      return v.parse(schema, JSON.parse(stringValue))
    } catch {
      return defaults
    }
  }

  const data = ref(getInitialValue(String(queryString.value)))

  function defaultUpdateQueryFn(data: v.InferOutput<T>): string | undefined {
    if (!deepEqual(data, defaults)) return JSON.stringify(data)
    return undefined
  }

  const updateFn = updateQueryFn ?? defaultUpdateQueryFn

  syncRef(data, queryString, {
    direction: 'ltr',
    deep: true,
    transform: {
      ltr: (left) => updateFn(left),
    },
  })

  return {
    data: data as Ref<v.InferOutput<T>>,
  }
}
