import type { SchemaFilterSeriesStatus } from '@/types/filter'
import type { InferOutput } from 'valibot'

export function schemaFilterSeriesStatusToConditions(
  filter: MaybeRefOrGetter<InferOutput<typeof SchemaFilterSeriesStatus>>,
) {
  const list = toValue(filter).v.map((it) => ({
    seriesStatus: {
      operator: it.i === 'e' ? 'isNot' : 'is',
      value: it.v,
    },
  }))

  if (toValue(filter).m === 'allOf')
    return {
      allOf: list,
    }
  else
    return {
      anyOf: list,
    }
}
