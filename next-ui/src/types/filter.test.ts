import { describe, expect, test } from 'vitest'
import * as v from 'valibot'
import {
  SchemaAnyAll,
  SchemaFilterCreators,
  SchemaFilterCreatorsRecord,
  SchemaFilterReadStatus,
  SchemaFilterSeriesStatus,
  SchemaFilterStrings,
  SchemaSeriesAgeRatings,
  SchemaSeriesReleaseYears,
  SchemaSeriesStatus,
} from '@/types/filter'

describe('schema any all', () => {
  test('anyOf', () => {
    const input = { m: 'anyOf' }
    const result = v.parse(SchemaAnyAll, input)

    expect(result).toStrictEqual(input)
  })

  test('allOf', () => {
    const input = { m: 'allOf' }
    const result = v.parse(SchemaAnyAll, input)

    expect(result).toStrictEqual(input)
  })

  test('other value throws error', () => {
    const input = { m: 'other' }

    expect(() => v.parse(SchemaAnyAll, input)).toThrowError()
  })

  test('defaults to anyOf', () => {
    const input = { m: 'anyOf' }
    const defaults = v.getDefaults(SchemaAnyAll)

    expect(defaults).toStrictEqual(input)
  })
})

describe('schema SchemaFilterAuthorsV2', () => {
  test('one role', () => {
    const input = { writer: { m: 'anyOf', v: [{ i: 'i', v: 'Mark+Lutz' }] } }
    const result = v.parse(SchemaFilterCreatorsRecord, input)

    expect(result).toStrictEqual(input)
  })

  test('multiple role', () => {
    const input = {
      anyrole: { m: 'anyOf', v: [] },
      writer: { m: 'anyOf', v: [{ i: 'i', v: 'Lewis+Carroll' }] },
      letterer: { m: 'anyOf', v: [{ i: 'i', v: 'Clem+Robins' }] },
    }
    const result = v.parse(SchemaFilterCreatorsRecord, input)

    expect(result).toStrictEqual(input)
  })
})

describe('schema series status', () => {
  test('correct value', () => {
    const input = 'ENDED'
    const result = v.parse(SchemaSeriesStatus, input)

    expect(result).toStrictEqual(input)
  })

  test('case insensitive', () => {
    const input = 'enDeD'
    const result = v.parse(SchemaSeriesStatus, input)

    expect(result).toStrictEqual('ENDED')
  })

  test('other value throws error', () => {
    const input = 'whatever'

    expect(() => v.parse(SchemaSeriesStatus, input)).toThrowError()
  })
})

describe('schema series release years', () => {
  test('correct value exact', () => {
    const input = { is: '1985' }
    const result = v.parse(SchemaSeriesReleaseYears, input)

    expect(result).toStrictEqual(input)
  })

  test('correct value min', () => {
    const input = { min: '1985' }
    const result = v.parse(SchemaSeriesReleaseYears, input)

    expect(result).toStrictEqual(input)
  })

  test('correct value max', () => {
    const input = { max: '1985' }
    const result = v.parse(SchemaSeriesReleaseYears, input)

    expect(result).toStrictEqual(input)
  })

  test('correct value is: any', () => {
    const input = { is: 'any' }
    const result = v.parse(SchemaSeriesReleaseYears, input)

    expect(result).toStrictEqual(input)
  })

  test('correct value is: none', () => {
    const input = { is: 'none' }
    const result = v.parse(SchemaSeriesReleaseYears, input)

    expect(result).toStrictEqual(input)
  })

  test('other value throws error', () => {
    const input = { is: '20254' }

    expect(() => v.parse(SchemaSeriesReleaseYears, input)).toThrowError()
  })
})

describe('schema series age ratings', () => {
  test('correct value exact', () => {
    const input = { is: 10 }
    const result = v.parse(SchemaSeriesAgeRatings, input)

    expect(result).toStrictEqual(input)
  })

  test('correct value min', () => {
    const input = { min: 12 }
    const result = v.parse(SchemaSeriesAgeRatings, input)

    expect(result).toStrictEqual(input)
  })

  test('correct value max', () => {
    const input = { max: 15 }
    const result = v.parse(SchemaSeriesAgeRatings, input)

    expect(result).toStrictEqual(input)
  })

  test('correct value is: any', () => {
    const input = { is: 'any' }
    const result = v.parse(SchemaSeriesAgeRatings, input)

    expect(result).toStrictEqual(input)
  })

  test('correct value is: none', () => {
    const input = { is: 'none' }
    const result = v.parse(SchemaSeriesAgeRatings, input)

    expect(result).toStrictEqual(input)
  })

  test('other value throws error', () => {
    const input = { is: '22' }

    expect(() => v.parse(SchemaSeriesAgeRatings, input)).toThrowError()
  })
})

describe('filter schemas have a default value', () => {
  test('SchemaFilterSeriesStatus', () => {
    const expected = { v: [] }
    const defaults = v.getDefaults(SchemaFilterSeriesStatus)

    expect(defaults).toStrictEqual(expected)
  })

  test('SchemaFilterReadStatus', () => {
    const expected = { v: [] }
    const defaults = v.getDefaults(SchemaFilterReadStatus)

    expect(defaults).toStrictEqual(expected)
  })

  test('SchemaFilterStrings', () => {
    const expected = { m: 'anyOf', v: [] }
    const defaults = v.getDefaults(SchemaFilterStrings)

    expect(defaults).toStrictEqual(expected)
  })

  test('SchemaFilterAuthors', () => {
    const expected = { m: 'anyOf', v: [] }
    const defaults = v.getDefaults(SchemaFilterCreators)

    expect(defaults).toStrictEqual(expected)
  })

  test('SchemaFilterAuthorsV2', () => {
    const expected = {}
    const defaults = v.getDefaults(SchemaFilterCreatorsRecord)

    expect(defaults).toStrictEqual(expected)
  })

  test('SchemaSeriesReleaseYears', () => {
    const expected = { is: undefined, min: undefined, max: undefined }
    const defaults = v.getDefaults(SchemaSeriesReleaseYears)

    expect(defaults).toStrictEqual(expected)
  })

  test('SchemaSeriesAgeRatings', () => {
    const expected = { is: undefined, min: undefined, max: undefined }
    const defaults = v.getDefaults(SchemaSeriesAgeRatings)

    expect(defaults).toStrictEqual(expected)
  })
})
