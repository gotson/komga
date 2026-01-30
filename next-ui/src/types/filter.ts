import * as v from 'valibot'

export type AnyAll = 'anyOf' | 'allOf'

///////////////////////////////////////////////////////////////////////////
// This file contains all the valibot schemas for the Criteria API.
// Those objects end up serialized in the address bar in the query params,
// that's why the properties and values are kept to the shortest.
///////////////////////////////////////////////////////////////////////////

/**
 * Schema for criteria API with `anyOf` or `allOf` condition.
 */
export const SchemaAnyAll = v.object({
  /**
   * Shorthand for `mode`.
   */
  m: v.optional(v.picklist(['anyOf', 'allOf']), 'anyOf'),
})

const SchemaIncludeExclude = v.object({
  /**
   * Shorthand for `include`.
   *
   * Values can be: include (`i`) or exclude (`e`)
   */
  i: v.optional(v.picklist(['i', 'e'])),
})

export const SchemaSeriesStatus = v.object({
  ...SchemaIncludeExclude.entries,
  /**
   * Shorthand for `value`.
   */
  v: v.pipe(v.string(), v.toUpperCase(), v.picklist(['ENDED', 'ONGOING', 'ABANDONED', 'HIATUS'])),
})

export const SchemaString = v.object({
  ...SchemaIncludeExclude.entries,
  /**
   * Shorthand for `value`.
   */
  v: v.string(),
})

////////////////////////////////////////////////////
// All schema filters need to have a default value
////////////////////////////////////////////////////

/**
 * Schema for Series Status.
 */
export const SchemaFilterSeriesStatus = v.object({
  ...SchemaAnyAll.entries,
  /**
   * Shorthand for 'value'
   */
  v: v.optional(v.array(SchemaSeriesStatus), []),
})

/**
 * Schema for a list of string.
 * Can be used for tags, genre, sharing labels…
 */
export const SchemaFilterStrings = v.object({
  ...SchemaAnyAll.entries,
  /**
   * Shorthand for 'value'
   */
  v: v.optional(v.array(SchemaString), []),
})
