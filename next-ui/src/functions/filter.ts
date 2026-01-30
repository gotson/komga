import type { SchemaFilterSeriesStatus } from '@/types/filter'
import type { InferOutput } from 'valibot'

export function schemaFilterSeriesStatusToConditions(
  filter: InferOutput<typeof SchemaFilterSeriesStatus>,
) {
  const list = filter.v.map((it) => ({
    seriesStatus: {
      operator: it.i === 'e' ? 'isNot' : 'is',
      value: it.v,
    },
  }))

  if (filter.m === 'allOf')
    return {
      allOf: list,
    }
  else
    return {
      anyOf: list,
    }
}
