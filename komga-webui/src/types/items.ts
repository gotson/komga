import { bookThumbnailUrl, seriesThumbnailUrl } from '@/functions/urls'
import { VueRouter } from 'vue-router/types/router'

function plural (count: number, singular: string, plural: string) {
  return `${count} ${count === 1 ? singular : plural}`
}

export function createItem (item: BookDto | SeriesDto): Item<BookDto | SeriesDto> {
  if ('seriesId' in item) {
    return new BookItem(item)
  } else if ('libraryId' in item) {
    return new SeriesItem(item)
  } else {
    throw new Error('The given item type is not known!')
  }
}

export abstract class Item<T> {
  item: T;

  constructor (item: T) {
    this.item = item
  }

  subtitleProps (): Object {
    return {
      style: 'word-break: normal !important; height: 4em',
      'class': 'pa-2 pb-1 text--primary',
      'v-line-clamp': 2,
      title: this.title(),
    }
  }

  abstract thumbnailUrl(): string

  abstract title(): string

  abstract subtitle(): string

  abstract body(): string

  abstract goto(router: VueRouter): void
}

export class BookItem extends Item<BookDto> {
  thumbnailUrl (): string {
    return bookThumbnailUrl(this.item.id)
  }

  subtitle (): string {
    const m = this.item.metadata
    return `#${m.number} - ${m.title}`
  }

  title (): string {
    return this.item.metadata.title
  }

  body (): string {
    let c = this.item.media.pagesCount
    return `
       <div>${this.item.size}</div>
       <div>${plural(c, 'Page', 'Pages')}</div>
    `
  }

  goto (router: VueRouter): void {
    router.push({ name: 'browse-book', params: { bookId: this.item.id.toString() } })
  }
}

export class SeriesItem extends Item<SeriesDto> {
  thumbnailUrl (): string {
    return seriesThumbnailUrl(this.item.id)
  }

  subtitle (): string {
    return ''
  }

  title (): string {
    return this.item.metadata.title
  }

  body (): string {
    let c = this.item.booksCount
    return `<span>${plural(c, 'Book', 'Books')}</span>`
  }

  goto (router: VueRouter): void {
    router.push({ name: 'browse-series', params: { seriesId: this.item.id.toString() } })
  }
}
