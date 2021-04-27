import {PageDtoWithUrl} from "@/types/komga-books"
import {PagedReaderLayout} from "@/types/enum-reader"
import {isPageLandscape} from "@/functions/page"
import {cloneDeep, drop, dropRight} from 'lodash'


export function buildSpreads(pages: PageDtoWithUrl[], pageLayout: PagedReaderLayout): PageDtoWithUrl[][] {
  if (pages.length === 0) return []
  if (pageLayout !== PagedReaderLayout.SINGLE_PAGE) {
    const spreads = []
    let pagesClone: PageDtoWithUrl[]
    let lastPage = undefined
    if (pageLayout === PagedReaderLayout.DOUBLE_PAGES) {
      spreads.push([pages[0]])
      pagesClone = drop(pages)
      if (pagesClone.length > 0) lastPage = dropRight(pagesClone)
    } else {
      pagesClone = cloneDeep(pages)
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
    if (lastPage) spreads.push(lastPage)
    return spreads
  } else {
    return pages.map(p => [p])
  }
}
