import { describe, expect, test } from 'vitest'
import * as v from 'valibot'
import {
  SchemaAnyAll,
  SchemaFilterAuthors,
  SchemaFilterSeriesStatus,
  SchemaFilterStrings,
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

describe('filter schemas have a default value', () => {
  test('SchemaFilterSeriesStatus', () => {
    const expected = { v: [] }
    const defaults = v.getDefaults(SchemaFilterSeriesStatus)

    expect(defaults).toStrictEqual(expected)
  })

  test('SchemaFilterStrings', () => {
    const expected = { m: 'anyOf', v: [] }
    const defaults = v.getDefaults(SchemaFilterStrings)

    expect(defaults).toStrictEqual(expected)
  })

  test('SchemaFilterAuthors', () => {
    const expected = { m: 'anyOf', v: [] }
    const defaults = v.getDefaults(SchemaFilterAuthors)

    expect(defaults).toStrictEqual(expected)
  })

  test('SchemaSeriesReleaseYears', () => {
    const expected = { is: undefined, min: undefined, max: undefined }
    const defaults = v.getDefaults(SchemaSeriesReleaseYears)

    expect(defaults).toStrictEqual(expected)
  })
})
