import * as v from 'valibot'
import { defineMessage, type MessageDescriptor } from 'vue-intl'

export const filterMessages: Record<string, MessageDescriptor> = {
  any: defineMessage({
    description: 'Filter values: any',
    defaultMessage: 'Any',
    id: 'bDNv5+',
  }),
}

export const filterKeys = {
  context: Symbol() as InjectionKey<{
    library_id?: string[]
    collection_id?: string
    series_id?: string
    readlist_id?: string
  }>,
}

export type AnyAll = 'anyOf' | 'allOf'

///////////////////////////////////////////////////////////////////////////
// This file contains all the valibot schemas for the Criteria API.
// Those objects end up serialized in the address bar in the query params,
// that's why the properties and values are kept to the shortest.
///////////////////////////////////////////////////////////////////////////

/**
 * Schema for criteria API with `anyOf` or `allOf` condition.
 */
export const SchemaAnyAll = v.strictObject({
  /**
   * Shorthand for `mode`.
   */
  m: v.optional(v.picklist(['anyOf', 'allOf']), 'anyOf'),
})

const SchemaIncludeExclude = v.strictObject({
  /**
   * Shorthand for `include`.
   *
   * Values can be: include (`i`) or exclude (`e`)
   */
  i: v.optional(v.picklist(['i', 'e'])),
})

export const SchemaSeriesStatus = v.pipe(
  v.string(),
  v.toUpperCase(),
  v.picklist(['ENDED', 'ONGOING', 'ABANDONED', 'HIATUS']),
)

export const SchemaString = v.strictObject({
  ...SchemaIncludeExclude.entries,
  /**
   * Shorthand for `value`.
   */
  v: v.string(),
})

export const SchemaAnyNone = v.strictObject({
  a: v.optional(v.picklist(['any', 'none'])),
})

export const SchemaAuthor = v.union([SchemaString, SchemaAnyNone])

////////////////////////////////////////////////////
// All schema filters need to have a default value
////////////////////////////////////////////////////

function createSchemaFilterAnyAll<T extends v.GenericSchema>(schema: T) {
  return v.strictObject({
    ...SchemaAnyAll.entries,
    /**
     * Shorthand for 'value'
     */
    v: v.optional(v.array(schema), []),
  })
}

function createSchemaFilterArray<T extends v.GenericSchema>(schema: T) {
  return v.strictObject({
    /**
     * Shorthand for 'value'
     */
    v: v.optional(v.array(schema), []),
  })
}

/**
 * Schema for Series Status.
 */
export const SchemaFilterSeriesStatus = createSchemaFilterArray(SchemaSeriesStatus)

/**
 * Schema for a list of string.
 * Can be used for tags, genre, sharing labels…
 */
export const SchemaFilterStrings = createSchemaFilterAnyAll(SchemaString)

/**
 * Schema for authors.
 */
export const SchemaFilterAuthors = createSchemaFilterAnyAll(SchemaAuthor)

export const SchemaFilterAnyAll = createSchemaFilterAnyAll(v.unknown())
export type FilterTypeAnyAll = v.InferOutput<typeof SchemaFilterAnyAll>

export const SchemaFilterArray = createSchemaFilterArray(v.unknown())
export type FilterTypeSimpleList = v.InferOutput<typeof SchemaFilterArray>

export type FilterType = FilterTypeAnyAll | FilterTypeSimpleList
