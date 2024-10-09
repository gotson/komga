import {buildSpreads} from '@/functions/book-spreads'
import {PagedReaderLayout} from '@/types/enum-reader'
import {PageDtoWithUrl} from '@/types/komga-books'

describe('Single Page', () => {
  const pageLayout = PagedReaderLayout.SINGLE_PAGE

  test('given no pages then it should return no spreads', () => {
    const pages = [] as PageDtoWithUrl[]

    const spreads = buildSpreads(pages, pageLayout)

    expect(spreads.length).toEqual(0)
  })

  test('given single page then it should return single spread with single page', () => {
    const pages = [
      {
        number: 1,
      } as PageDtoWithUrl,
    ] as PageDtoWithUrl[]

    const spreads = buildSpreads(pages, pageLayout)

    expect(spreads.length).toEqual(1)
    expect(spreads[0].length).toEqual(1)
    expect(spreads[0][0].number).toEqual(1)
  })
})

describe('Double Pages', () => {
  const pageLayout = PagedReaderLayout.DOUBLE_PAGES

  test('given no pages then it should return no spreads', () => {
    const pages = [] as PageDtoWithUrl[]

    const spreads = buildSpreads(pages, pageLayout)

    expect(spreads.length).toEqual(0)
  })

  test('given single page then it should return single spread with single page', () => {
    const pages = [
      {
        number: 1,
      } as PageDtoWithUrl,
    ] as PageDtoWithUrl[]

    const spreads = buildSpreads(pages, pageLayout)

    expect(spreads.length).toEqual(1)
    expect(spreads[0].length).toEqual(2)
    expect(spreads[0][1].number).toEqual(1)
    expect(spreads[0][0].number).toEqual(0) // empty page
  })

  test('given even pages then it should return correct spreads', () => {
    const pages = [
      {number: 1} as PageDtoWithUrl,
      {number: 2} as PageDtoWithUrl,
      {number: 3} as PageDtoWithUrl,
      {number: 4} as PageDtoWithUrl,
      {number: 5} as PageDtoWithUrl,
      {number: 6} as PageDtoWithUrl,
    ] as PageDtoWithUrl[]

    const spreads = buildSpreads(pages, pageLayout)

    expect(spreads.length).toEqual(4)

    expect(spreads[0].length).toEqual(2)
    expect(spreads[0][1].number).toEqual(1)
    expect(spreads[0][0].number).toEqual(0)  // empty page

    expect(spreads[1].length).toEqual(2)
    expect(spreads[1][0].number).toEqual(2)
    expect(spreads[1][1].number).toEqual(3)

    expect(spreads[2].length).toEqual(2)
    expect(spreads[2][0].number).toEqual(4)
    expect(spreads[2][1].number).toEqual(5)

    expect(spreads[3].length).toEqual(2)
    expect(spreads[3][0].number).toEqual(6)
    expect(spreads[3][1].number).toEqual(0) // empty page
  })

  test('given odd pages then it should return correct spreads', () => {
    const pages = [
      {number: 1} as PageDtoWithUrl,
      {number: 2} as PageDtoWithUrl,
      {number: 3} as PageDtoWithUrl,
      {number: 4} as PageDtoWithUrl,
      {number: 5} as PageDtoWithUrl,
    ] as PageDtoWithUrl[]

    const spreads = buildSpreads(pages, pageLayout)
    // Expecting [E, 1], [2, 3], [4, 5]
    expect(spreads.length).toEqual(3)

    expect(spreads[0].length).toEqual(2)
    expect(spreads[0][0].number).toEqual(0) // empty page
    expect(spreads[0][1].number).toEqual(1)

    expect(spreads[1].length).toEqual(2)
    expect(spreads[1][0].number).toEqual(2)
    expect(spreads[1][1].number).toEqual(3)

    expect(spreads[2].length).toEqual(2)
    expect(spreads[2][0].number).toEqual(4)
    expect(spreads[2][1].number).toEqual(5)
  })
})

describe('Double Pages No Cover', () => {
  const pageLayout = PagedReaderLayout.DOUBLE_NO_COVER

  test('given no pages then it should return no spreads', () => {
    const pages = [] as PageDtoWithUrl[]

    const spreads = buildSpreads(pages, pageLayout)

    expect(spreads.length).toEqual(0)
  })

  test('given single page then it should return single spread with single page', () => {
    const pages = [
      {
        number: 1,
      } as PageDtoWithUrl,
    ] as PageDtoWithUrl[]

    const spreads = buildSpreads(pages, pageLayout)

    expect(spreads.length).toEqual(1)
    expect(spreads[0].length).toEqual(2)
    expect(spreads[0][0].number).toEqual(1)
    expect(spreads[0][1].number).toEqual(0) // empty page
  })
})
