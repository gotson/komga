import { describe, expect, test } from 'vitest'
import type { EntityKind } from '@/functions/entity'
import { getCommonActions } from '@/functions/selection'
import { ActionName, selectionActions } from '@/types/action/action'

describe('selection actions', () => {
  test('one kind', () => {
    const kinds: EntityKind[] = ['book']
    const expected: ActionName[] = selectionActions.book

    expect(getCommonActions(kinds)).toEqual(expected)
  })

  test('undefined', () => {
    const kinds: (EntityKind | undefined)[] = [undefined]
    const expected: ActionName[] = []

    expect(getCommonActions(kinds)).toEqual(expected)
  })

  test('undefined and collection', () => {
    const kinds: (EntityKind | undefined)[] = [undefined, 'collection']
    const expected: ActionName[] = []

    expect(getCommonActions(kinds)).toEqual(expected)
  })

  test('book and collection', () => {
    const kinds: EntityKind[] = ['book', 'collection']
    const expected: ActionName[] = [ActionName.DELETE]

    expect(getCommonActions(kinds)).toEqual(expected)
  })

  test('book and series', () => {
    const kinds: EntityKind[] = ['book', 'series']
    const expected: ActionName[] = [
      ActionName.MARK_READ,
      ActionName.MARK_UNREAD,
      ActionName.ADD_TO_READLIST,
      ActionName.REFRESH_METADATA,
      ActionName.ANALYZE,
      ActionName.DELETE,
    ]

    expect(getCommonActions(kinds)).toEqual(expected)
  })

  test('all', () => {
    const kinds: EntityKind[] = ['book', 'series', 'collection', 'readlist']
    const expected: ActionName[] = [ActionName.DELETE]

    expect(getCommonActions(kinds)).toEqual(expected)
  })
})
