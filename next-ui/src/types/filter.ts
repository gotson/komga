import * as v from 'valibot'

export type AnyAll = 'anyOf' | 'allOf'

export const SchemaAnyAll = v.object({
  m: v.optional(v.picklist(['anyOf', 'allOf']), 'anyOf'),
})

const SchemaIncludeExclude = v.object({ i: v.optional(v.picklist(['i', 'e'])) })

const SchemaSeriesStatus = v.object({
  ...SchemaIncludeExclude.entries,
  v: v.optional(v.picklist(['ENDED', 'ONGOING', 'ABANDONED', 'HIATUS'])),
})

export const SchemaFilterSeriesStatus = v.object({
  ...SchemaAnyAll.entries,
  v: v.fallback(v.optional(v.array(SchemaSeriesStatus), []), []),
})

export const FilterSeriesGenre = v.object({
  ...SchemaAnyAll.entries,
  value: v.array(v.string()),
})
