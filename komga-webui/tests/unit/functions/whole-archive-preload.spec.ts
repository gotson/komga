import {computePreloadOrder} from '@/functions/whole-archive-preload'

describe('computePreloadOrder', () => {
  test('empty book returns empty order', () => {
    expect(computePreloadOrder(1, 0)).toEqual([])
  })

  test('starting at first page goes forward through the whole book', () => {
    expect(computePreloadOrder(1, 5)).toEqual([0, 1, 2, 3, 4])
  })

  test('starting mid-book prioritises forward then wraps to start', () => {
    expect(computePreloadOrder(3, 5)).toEqual([2, 3, 4, 0, 1])
  })

  test('starting at last page wraps cleanly', () => {
    expect(computePreloadOrder(5, 5)).toEqual([4, 0, 1, 2, 3])
  })

  test('non-finite current page falls back to page 1', () => {
    expect(computePreloadOrder(NaN, 3)).toEqual([0, 1, 2])
    expect(computePreloadOrder(undefined as unknown as number, 3)).toEqual([0, 1, 2])
  })

  test('out-of-range current page is clamped', () => {
    expect(computePreloadOrder(0, 3)).toEqual([0, 1, 2])
    expect(computePreloadOrder(99, 3)).toEqual([2, 0, 1])
  })
})
