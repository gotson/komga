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
      const firstPage = pagesClone.shift() as PageDtoWithUrl
      if (isPageLandscape(firstPage))
        spreads.push([firstPage] as PageDtoWithUrl[])
      else
        spreads.push([createEmptyPage(firstPage), firstPage] as PageDtoWithUrl[])
      if (pagesClone.length > 0) {
        const lastPage = pagesClone.pop() as PageDtoWithUrl
        if(isPageLandscape(lastPage))
          lastPages = [lastPage] as PageDtoWithUrl[]
        else
          lastPages = [lastPage, createEmptyPage(lastPage)] as PageDtoWithUrl[]
      }
    }
    while (pagesClone.length > 0) {
      const p = pagesClone.shift() as PageDtoWithUrl
      if (isPageLandscape(p)) {
        spreads.push([p])
      } else {
        if (pagesClone.length > 0) {
          const p2 = pagesClone.shift() as PageDtoWithUrl
          if (isPageLandscape(p2)) {
            spreads.push([p, createEmptyPage(p)])
            spreads.push([p2])
          } else {
            spreads.push([p, p2])
          }
        } else {
          spreads.push([p, createEmptyPage(p)])
        }
      }
    }
    if (lastPages) spreads.push(lastPages)
    return spreads
  } else {
    return pages.map(p => [p])
  }
}

function createEmptyPage(page: PageDtoWithUrl): PageDtoWithUrl {
  return {
    url: createTransparentDataUrl(page?.width || 20, page?.height || 30),
    number: 0,
  } as PageDtoWithUrl
}

function createTransparentDataUrl(w: number, h: number): string {
  const canvas = document.createElement('canvas')
  canvas.width = w
  canvas.height = h

  const ctx = canvas.getContext('2d')
  if (ctx) {
    ctx.fillStyle = 'rgb(0,0,0,0)'
    ctx.fillRect(0, 0, w, h)
  }

  return canvas.toDataURL()
}
