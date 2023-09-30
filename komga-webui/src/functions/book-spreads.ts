import {PageDtoWithUrl} from '@/types/komga-books'
import {PagedReaderLayout} from '@/types/enum-reader'
import {isPageLandscape} from '@/functions/page'


export function buildSpreads(pages: PageDtoWithUrl[], pageLayout: PagedReaderLayout): PageDtoWithUrl[][] {
  switch (pageLayout) {
    case PagedReaderLayout.DOUBLE_PAGES:    return createDoublePages(pages)
    case PagedReaderLayout.DOUBLE_NO_COVER: return createDoubleNoCover(pages)
    default:                                return createSinglePage(pages)
  }
}

function createSinglePage(pages: PageDtoWithUrl[]): PageDtoWithUrl[][] {
  return pages.length !== 0 ? pages.map(p => [p]) : []
}

function createDoublePages(pages: PageDtoWithUrl[]): PageDtoWithUrl[][] {
  const spreads = createDoubleNoCover(pages.slice(1, -1))
  const n = pages.length
  if (n > 0) {
    spreads.unshift(isPageLandscape(pages[0]) ? [pages[0]] : [createEmptyPage(pages[0]), pages[0]])
  }
  if (n > 1) {
    spreads.push(isPageLandscape(pages[n - 1]) ? [pages[n-1]] : [createEmptyPage(pages[n-1]), pages[n-1]])
  }
  return spreads
}

function createDoubleNoCover(pages: PageDtoWithUrl[]): PageDtoWithUrl[][] {
  const n = pages.length
  if (n === 0) return []
  const spreads = [] as PageDtoWithUrl[][]
  for (let i = 0; i < n; ++i) {
    const p1 = pages[i] as PageDtoWithUrl
    const p2 = i !== n - 1 ? pages[i + 1] as PageDtoWithUrl : createEmptyPage(p1)
    isPageLandscape(p1) ? spreads.push([p1])                      :
    isPageLandscape(p2) ? spreads.push([p1, createEmptyPage(p1)]) :
                         (spreads.push([p1, p2]), ++i)
  }
  return spreads
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
