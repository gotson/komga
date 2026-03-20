import {
  SchemaAnyNone,
  SchemaFilterAuthors,
  type SchemaFilterSeriesStatus,
  SchemaFilterStrings,
} from '@/types/filter'
import type { InferOutput } from 'valibot'
import * as v from 'valibot'

export function schemaFilterSeriesStatusToConditions(
  filter: InferOutput<typeof SchemaFilterSeriesStatus>,
) {
  const list = filter.v.map((it) => ({
    seriesStatus: {
      operator: 'is',
      value: it,
    },
  }))

  return {
    anyOf: list,
  }
}

export function schemaFilterAuthorsToConditions(
  filter: InferOutput<typeof SchemaFilterAuthors>,
  role?: string,
) {
  const list = filter.v.map((it) => {
    if (v.is(SchemaAnyNone, it)) {
      return {
        author: {
          operator: it.a === 'any' ? 'is' : 'isNot',
          value: {
            role: role,
          },
        },
      }
    } else {
      return {
        author: {
          operator: it.i === 'e' ? 'isNot' : 'is',
          value: {
            name: it.v,
            role: role,
          },
        },
      }
    }
  })

  if (filter.m === 'allOf')
    return {
      allOf: list,
    }
  else
    return {
      anyOf: list,
    }
}

export function schemaFilterStringToConditions(
  filter: InferOutput<typeof SchemaFilterStrings>,
  key: string,
  nullable: boolean,
) {
  const list = filter.v.map((it) => {
    if (v.is(SchemaAnyNone, it)) {
      if (nullable)
        return {
          [key]: {
            operator: it.a === 'any' ? 'isNotNull' : 'isNull',
          },
        }
      else
        return {
          [key]: {
            operator: it.a === 'any' ? 'isNot' : 'is',
            value: '',
          },
        }
    } else {
      return {
        [key]: {
          operator: it.i === 'e' ? 'isNot' : 'is',
          value: it.v,
        },
      }
    }
  })

  if (filter.m === 'allOf')
    return {
      allOf: list,
    }
  else
    return {
      anyOf: list,
    }
}
