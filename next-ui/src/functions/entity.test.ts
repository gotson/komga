import { describe, expect, test } from 'vitest'
import { mockBook } from '@/mocks/api/handlers/books'
import { isBook, isCollection, isReadList, isSeries, resolveEntityKind } from '@/functions/entity'
import { mockSeries1 } from '@/mocks/api/handlers/series'
import { mockCollection } from '@/mocks/api/handlers/collections'
import { mockReadList1 } from '@/mocks/api/handlers/readlists'

describe('entity type guards', () => {
  test('book', () => {
    const item = mockBook

    expect(isBook(item)).toBeTruthy()
    expect(isSeries(item)).toBeFalsy()
    expect(isCollection(item)).toBeFalsy()
    expect(isReadList(item)).toBeFalsy()

    expect(resolveEntityKind(item)).toStrictEqual('book')
  })

  test('series', () => {
    const item = mockSeries1

    expect(isBook(item)).toBeFalsy()
    expect(isSeries(item)).toBeTruthy()
    expect(isCollection(item)).toBeFalsy()
    expect(isReadList(item)).toBeFalsy()

    expect(resolveEntityKind(item)).toStrictEqual('series')
  })

  test('collection', () => {
    const item = mockCollection

    expect(isBook(item)).toBeFalsy()
    expect(isSeries(item)).toBeFalsy()
    expect(isCollection(item)).toBeTruthy()
    expect(isReadList(item)).toBeFalsy()

    expect(resolveEntityKind(item)).toStrictEqual('collection')
  })

  test('readlist', () => {
    const item = mockReadList1

    expect(isBook(item)).toBeFalsy()
    expect(isSeries(item)).toBeFalsy()
    expect(isCollection(item)).toBeFalsy()
    expect(isReadList(item)).toBeTruthy()

    expect(resolveEntityKind(item)).toStrictEqual('readlist')
  })

  test('other', () => {
    const item = { object: 'item' }

    expect(isBook(item)).toBeFalsy()
    expect(isSeries(item)).toBeFalsy()
    expect(isCollection(item)).toBeFalsy()
    expect(isReadList(item)).toBeFalsy()

    expect(resolveEntityKind(item)).toBeUndefined()
  })
})
