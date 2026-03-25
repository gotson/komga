import {
  type FilterIncludeExclude,
  SchemaAnyNone,
  SchemaFilterAuthors,
  SchemaFilterReadStatus,
  type SchemaFilterSeriesStatus,
  SchemaFilterStrings,
  SchemaSeriesAgeRatings,
  SchemaSeriesReleaseYears,
} from '@/types/filter'
import type { InferOutput } from 'valibot'
import * as v from 'valibot'

export function schemaFilterSeriesStatusToConditions(
  filter: InferOutput<typeof SchemaFilterSeriesStatus>,
) {
  if (filter.v.length === 0) return null
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
  if (filter.v.length === 0) return null
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

export function schemaFilterReadStatusToConditions(
  filter: InferOutput<typeof SchemaFilterReadStatus>,
) {
  if (filter.v.length === 0) return null
  const list = filter.v.map((it) => {
    return {
      readStatus: {
        operator: it.i === 'e' ? 'isNot' : 'is',
        value: it.v,
      },
    }
  })
  return {
    anyOf: list,
  }
}

export function schemaFilterIncludeExcludeToConditions(filter: FilterIncludeExclude, key: string) {
  if (!filter.i) return null
  return {
    [key]: {
      operator: filter.i === 'i' ? 'isTrue' : 'isFalse',
    },
  }
}

export function schemaFilterStringToConditions(
  filter: InferOutput<typeof SchemaFilterStrings>,
  key: string,
  nullable: boolean,
) {
  if (filter.v.length === 0) return null
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

export function schemaFilterReleaseYearToConditions(
  filter: InferOutput<typeof SchemaSeriesReleaseYears>,
) {
  const conds = []
  if (filter.is === 'any') {
    conds.push({
      releaseDate: {
        operator: 'isNotNull',
      },
    })
  } else if (filter.is === 'none') {
    conds.push({
      releaseDate: {
        operator: 'isNull',
      },
    })
  } else {
    if (!!filter.is || !!filter.min) {
      const year = Number(filter.is || filter.min)
      conds.push({
        releaseDate: {
          operator: 'after',
          dateTime: `${(year - 1).toString().padStart(4, '0')}-12-31T12:00:00Z`,
        },
      })
    }
    if (!!filter.is || !!filter.max) {
      const year = Number(filter.is || filter.max)
      conds.push({
        releaseDate: {
          operator: 'before',
          dateTime: `${(year + 1).toString().padStart(4, '0')}-01-01T12:00:00Z`,
        },
      })
    }
  }
  if (conds.length === 0) return null
  return {
    allOf: conds,
  }
}

export function schemaFilterAgeRatingToConditions(
  filter: InferOutput<typeof SchemaSeriesAgeRatings>,
) {
  const conds = []
  if (filter.is === 'any') {
    conds.push({
      ageRating: {
        operator: 'isNotNull',
      },
    })
  } else if (filter.is === 'none') {
    conds.push({
      ageRating: {
        operator: 'isNull',
      },
    })
  } else if (!!filter.is) {
    conds.push({
      ageRating: {
        operator: 'is',
        value: filter.is,
      },
    })
  } else {
    if (!!filter.min) {
      conds.push({
        ageRating: {
          operator: 'greaterThan',
          value: filter.min,
        },
      })
    }
    if (!!filter.max) {
      conds.push({
        ageRating: {
          operator: 'lessThan',
          value: filter.max,
        },
      })
    }
  }
  if (conds.length === 0) return null
  return {
    allOf: conds,
  }
}
