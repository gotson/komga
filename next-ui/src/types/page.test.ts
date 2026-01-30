import { describe, expect, test } from 'vitest'
import * as v from 'valibot'
import { SchemaStrictlyPositive } from '@/types/page'

describe('pagination composable', () => {
  test('page=1', () => {
    const result = v.parse(SchemaStrictlyPositive, '1')

    expect(result).toStrictEqual(1)
  })

  test('page=5', () => {
    const result = v.parse(SchemaStrictlyPositive, '5')

    expect(result).toStrictEqual(5)
  })

  test('page=0', () => {
    const result = v.parse(SchemaStrictlyPositive, '0')

    expect(result).toStrictEqual(1)
  })

  test('page=-25', () => {
    const result = v.parse(SchemaStrictlyPositive, '')

    expect(result).toStrictEqual(1)
  })

  test('page=string', () => {
    const result = v.parse(SchemaStrictlyPositive, 'some string')

    expect(result).toStrictEqual(1)
  })
})
