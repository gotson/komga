import KomgaBooksService from '@/services/komga-books.service'
import { bookPageUrl } from '@/functions/urls'
import { checkWebpFeature } from '@/functions/check-webp'

const SUPPORTED_MEDIA_TYPES = ['image/jpeg', 'image/png', 'image/gif']
checkWebpFeature('lossy', (feature, isSupported) => {
  if (isSupported) {
    SUPPORTED_MEDIA_TYPES.push('image/webp')
  }
})
const convertTo: string = 'jpeg'

export interface Page {
  url: string
  number: number
}

export interface PageSpread {
  pages: Page[]
}

export interface PageSpreads {
  spreads: PageSpread[]
  indexOf: Map<number, number>
}

function getPageUrl (bookId: string, page: PageDto): string {
  if (!SUPPORTED_MEDIA_TYPES.includes(page.mediaType)) {
    return bookPageUrl(bookId, page.number, convertTo)
  } else {
    return bookPageUrl(bookId, page.number)
  }
}

function fromPageDtos (bookId: string, ...pageDtos: PageDto[]): PageSpread {
  const pages = pageDtos.map((p) => ({ url: getPageUrl(bookId, p), number: p.number }))
  return { pages: pages }
}

export abstract class PageSpreadBuilder {
  $komgaBooks: KomgaBooksService

  constructor ($komgaBooks: KomgaBooksService) {
    this.$komgaBooks = $komgaBooks
  }

  async getSpreads (book: BookDto, pages: PageDto[]): Promise<PageSpreads> {
    let pagesCopy = [...pages]
    return this.build(book, pagesCopy)
  }

  protected static buildIndexMap (spreads: PageSpread[]): Map<number, number> {
    let m = new Map<number, number>()
    for (let i = 0; i < spreads.length; i++) {
      const spread = spreads[i]
      for (let page of spread.pages) {
        m.set(page.number, i)
      }
    }
    return m
  }

  protected abstract build(book: BookDto, pageDtos: PageDto[]): PageSpreads
}

export class SingleSpreadBuilder extends PageSpreadBuilder {
  protected build (book: BookDto, pageDtos: PageDto[]): PageSpreads {
    let spreads: PageSpread[] = pageDtos.map((p) => fromPageDtos(book.id, p))
    return { spreads, indexOf: PageSpreadBuilder.buildIndexMap(spreads) } as PageSpreads
  }
}

export class DoubleSpreadBuilder extends PageSpreadBuilder {
  protected build (book: BookDto, pageDtos: PageDto[]): PageSpreads {
    let spreads = [] as PageSpread[]

    // First page
    spreads.push(fromPageDtos(book.id, pageDtos.shift()!!))
    // Last page
    let last = fromPageDtos(book.id, pageDtos.pop()!!)

    while (pageDtos.length > 0) {
      if (pageDtos.length > 1) {
        // Double up
        spreads.push(fromPageDtos(book.id, pageDtos.shift()!!, pageDtos.shift()!!))
      } else {
        spreads.push(fromPageDtos(book.id, pageDtos.shift()!!))
      }
    }
    spreads.push(last)

    return {
      spreads,
      indexOf: PageSpreadBuilder.buildIndexMap(spreads),
    }
  }
}
// FIXME unused
export class WebToonBuilder extends PageSpreadBuilder {
  protected build (book: BookDto, pageDtos: PageDto[]): PageSpreads {
    let spreads = [ fromPageDtos(book.id, ...pageDtos) ]
    return { spreads, indexOf: PageSpreadBuilder.buildIndexMap(spreads) }
  }
}
