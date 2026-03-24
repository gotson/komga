import { describe, expect, test } from 'vitest'
import { PageRequest } from '@/types/PageRequest'

describe('PageRequest destructuring', () => {
  test('spread', () => {
    const input = new PageRequest(5, 10, [{ key: 'title', order: 'asc' }], false)
    const p = { ...input }

    expect(p.page).toStrictEqual(5)
    expect(p.size).toStrictEqual(10)
    expect(p.sort).toStrictEqual(['title,asc'])
    expect(p.unpaged).toStrictEqual(false)
  })

  test('default constructor', () => {
    const input = new PageRequest(5, 10, [{ key: 'title', order: 'asc' }], false)
    const { page, size, sort, unpaged } = input

    expect(page).toStrictEqual(5)
    expect(size).toStrictEqual(10)
    expect(sort).toStrictEqual(['title,asc'])
    expect(unpaged).toStrictEqual(false)
  })

  test('multiple sorts', () => {
    const input = new PageRequest(
      5,
      10,
      [{ key: 'title', order: 'asc' }, { key: 'title2', order: 'desc' }, { key: 'title3' }],
      false,
    )
    const { page, size, sort, unpaged } = input

    expect(page).toStrictEqual(5)
    expect(size).toStrictEqual(10)
    expect(sort).toStrictEqual(['title,asc', 'title2,desc', 'title3'])
    expect(unpaged).toStrictEqual(false)
  })

  test('unpaged', () => {
    const input = PageRequest.Unpaged()
    const { page, size, sort, unpaged } = input

    expect(page).toBeUndefined()
    expect(size).toBeUndefined()
    expect(sort).toBeUndefined()
    expect(unpaged).toStrictEqual(true)
  })

  test('zero', () => {
    const input = PageRequest.Zero()
    const { page, size, sort, unpaged } = input

    expect(page).toBeUndefined()
    expect(size).toStrictEqual(0)
    expect(sort).toBeUndefined()
    expect(unpaged).toBeUndefined()
  })

  test('fromPagesize paged', () => {
    const input = PageRequest.FromPageSize(10, 5, [{ key: 'title', order: 'asc' }])
    const { page, size, sort, unpaged } = input

    expect(page).toStrictEqual(5)
    expect(size).toStrictEqual(10)
    expect(sort).toStrictEqual(['title,asc'])
    expect(unpaged).toStrictEqual(false)
  })

  test('fromPagesize unpaged', () => {
    const input = PageRequest.FromPageSize('unpaged', 5, [{ key: 'title', order: 'asc' }])
    const { page, size, sort, unpaged } = input

    expect(page).toStrictEqual(5)
    expect(size).toBeUndefined()
    expect(sort).toStrictEqual(['title,asc'])
    expect(unpaged).toStrictEqual(true)
  })
})
