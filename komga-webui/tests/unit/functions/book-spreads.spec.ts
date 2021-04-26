import {buildSpreads} from "@/functions/book-spreads";
import {PagedReaderLayout} from "@/types/enum-reader";
import {PageDtoWithUrl} from "@/types/komga-books";

describe("Single Page", () => {
  const pageLayout = PagedReaderLayout.SINGLE_PAGE

  test("given no pages then it should return no spreads", () => {
    const pages = [] as PageDtoWithUrl[]

    const spreads = buildSpreads(pages, pageLayout)

    expect(spreads.length).toEqual(0)
  })

  test("given single page then it should return single spread with single page", () => {
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

describe("Double Pages", () => {
  const pageLayout = PagedReaderLayout.DOUBLE_PAGES

  test("given no pages then it should return no spreads", () => {
    const pages = [] as PageDtoWithUrl[]

    const spreads = buildSpreads(pages, pageLayout)

    expect(spreads.length).toEqual(0)
  })

  test("given single page then it should return single spread with single page", () => {
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

describe("Double Pages No Cover", () => {
  const pageLayout = PagedReaderLayout.DOUBLE_NO_COVER

  test("given no pages then it should return no spreads", () => {
    const pages = [] as PageDtoWithUrl[]

    const spreads = buildSpreads(pages, pageLayout)

    expect(spreads.length).toEqual(0)
  })

  test("given single page then it should return single spread with single page", () => {
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
