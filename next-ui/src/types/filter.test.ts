import { describe, expect, test } from 'vitest'
import * as v from 'valibot'
import { SchemaAnyAll } from '@/types/filter'

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
