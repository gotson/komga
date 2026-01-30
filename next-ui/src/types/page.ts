import * as v from 'valibot'

export type PageSize = 'unpaged' | number

export const SchemaStrictlyPositive = v.fallback(v.pipe(v.string(), v.toNumber(), v.minValue(1)), 1)
