import {PageDtoWithUrl} from '@/types/komga-books'
import {PagedReaderLayout} from '@/types/enum-reader'
import {isPageLandscape} from '@/functions/page'
import {cloneDeep} from 'lodash'


export function buildSpreads(pages: PageDtoWithUrl[], pageLayout: PagedReaderLayout): PageDtoWithUrl[][] {
  if (pages.length === 0) return []
  if (pageLayout !== PagedReaderLayout.SINGLE_PAGE) {
    const spreads = [] as PageDtoWithUrl[][]
    const pagesClone = cloneDeep(pages)
    let lastPages = undefined
    if (pageLayout === PagedReaderLayout.DOUBLE_PAGES) {
      spreads.push([pagesClone.shift()] as PageDtoWithUrl[])
      if (pagesClone.length > 0) lastPages = [pagesClone.pop()] as PageDtoWithUrl[]
    }
    while (pagesClone.length > 0) {
      const p = pagesClone.shift() as PageDtoWithUrl
      if (isPageLandscape(p)) {
        spreads.push([p])
      } else {
        if (pagesClone.length > 0) {
          const p2 = pagesClone.shift() as PageDtoWithUrl
          if (isPageLandscape(p2)) {
            spreads.push([p])
            spreads.push([p2])
          } else {
            spreads.push([p, p2])
          }
        } else {
          spreads.push([p])
        }
      }
    }
    if (lastPages) spreads.push(lastPages)
    return spreads
  } else {
    return pages.map(p => [p])
  }
}
