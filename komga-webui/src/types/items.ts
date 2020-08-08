import { bookThumbnailUrl, collectionThumbnailUrl, seriesThumbnailUrl } from '@/functions/urls'
import { RawLocation } from 'vue-router/types/router'

function plural (count: number, singular: string, plural: string) {
  return `${count} ${count === 1 ? singular : plural}`
}

export enum ItemTypes {
  BOOK, SERIES, COLLECTION
}

export function createItem (item: BookDto | SeriesDto | CollectionDto): Item<BookDto | SeriesDto | CollectionDto> {
  if ('seriesIds' in item) {
    return new CollectionItem(item)
  } else if ('seriesId' in item) {
    return new BookItem(item)
  } else if ('libraryId' in item) {
    return new SeriesItem(item)
  } else {
    throw new Error('The given item type is not known!')
  }
}

export abstract class Item<T> {
  item: T

  constructor (item: T) {
    this.item = item
  }

  subtitleProps (): Object {
    return {
      style: 'word-break: normal !important; height: 4em',
      'class': 'pa-2 pb-1 text--primary',
      title: this.title(),
    }
  }

  abstract type (): ItemTypes

  abstract thumbnailUrl (): string

  abstract title (): string

  abstract body (): string

  abstract to (): RawLocation
}

export class BookItem extends Item<BookDto> {
  thumbnailUrl (): string {
    return bookThumbnailUrl(this.item.id)
  }

  type (): ItemTypes {
    return ItemTypes.BOOK
  }

  title (): string {
    const m = this.item.metadata
    return `#${m.number} - ${m.title}`
  }

  body (): string {
    const c = this.item.media.pagesCount
    return `<span>${plural(c, 'Page', 'Pages')}</span>`
  }

  to (): RawLocation {
    return { name: 'browse-book', params: { bookId: this.item.id.toString() } }
  }
}

export class SeriesItem extends Item<SeriesDto> {
  thumbnailUrl (): string {
    return seriesThumbnailUrl(this.item.id)
  }

  type (): ItemTypes {
    return ItemTypes.SERIES
  }

  title (): string {
    return this.item.metadata.title
  }

  body (): string {
    const c = this.item.booksCount
    return `<span>${plural(c, 'Book', 'Books')}</span>`
  }

  to (): RawLocation {
    return { name: 'browse-series', params: { seriesId: this.item.id.toString() } }
  }
}

export class CollectionItem extends Item<CollectionDto> {
  thumbnailUrl (): string {
    return collectionThumbnailUrl(this.item.id)
  }

  type (): ItemTypes {
    return ItemTypes.COLLECTION
  }

  title (): string {
    return this.item.name
  }

  body (): string {
    const c = this.item.seriesIds.length
    return `<span>${c} Series</span>`
  }

  to (): RawLocation {
    return { name: 'browse-collection', params: { collectionId: this.item.id.toString() } }
  }
}
