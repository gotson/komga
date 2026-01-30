import { describe, expect, test } from 'vitest'
import * as v from 'valibot'
import { SchemaStrictlyPositive } from '@/types/page'

describe('schema strictly positive', () => {
  test('1', () => {
    const result = v.parse(SchemaStrictlyPositive, '1')

    expect(result).toStrictEqual(1)
  })

  test('5', () => {
    const result = v.parse(SchemaStrictlyPositive, '5')

    expect(result).toStrictEqual(5)
  })

  test('0', () => {
    const result = v.parse(SchemaStrictlyPositive, '0')

    expect(result).toStrictEqual(1)
  })

  test('-25', () => {
    const result = v.parse(SchemaStrictlyPositive, '')

    expect(result).toStrictEqual(1)
  })

  test('string', () => {
    const result = v.parse(SchemaStrictlyPositive, 'some string')

    expect(result).toStrictEqual(1)
  })
})
