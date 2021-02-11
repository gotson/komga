import {bookThumbnailUrl, collectionThumbnailUrl, readListThumbnailUrl, seriesThumbnailUrl} from '@/functions/urls'
import {RawLocation} from 'vue-router/types/router'
import {BookDto} from '@/types/komga-books'
import {SeriesDto} from "@/types/komga-series";
import i18n from "@/i18n";

export enum ItemTypes {
  BOOK, SERIES, COLLECTION, READLIST
}

export function createItem (item: BookDto | SeriesDto | CollectionDto | ReadListDto): Item<BookDto | SeriesDto | CollectionDto | ReadListDto> {
  if ('bookIds' in item) {
    return new ReadListItem(item)
  } else if ('seriesIds' in item) {
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

  abstract fabTo (): RawLocation
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
    return `<span>${i18n.tc('common.pages_n', this.item.media.pagesCount)}</span>`
  }

  to (): RawLocation {
    return {
      name: 'browse-book',
      params: { bookId: this.item.id },
      query: { context: this.item?.context?.origin, contextId: this.item?.context?.id },
    }
  }

  fabTo (): RawLocation {
    return {
      name: 'read-book',
      params: { bookId: this.item.id },
      query: { context: this.item?.context?.origin, contextId: this.item?.context?.id },
    }
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
    return `<span>${i18n.tc('common.books_n', this.item.booksCount)}</span>`
  }

  to (): RawLocation {
    return { name: 'browse-series', params: { seriesId: this.item.id.toString() } }
  }

  fabTo (): RawLocation {
    return undefined as unknown as RawLocation
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

  fabTo (): RawLocation {
    return undefined as unknown as RawLocation
  }
}

export class ReadListItem extends Item<ReadListDto> {
  thumbnailUrl (): string {
    return readListThumbnailUrl(this.item.id)
  }

  type (): ItemTypes {
    return ItemTypes.READLIST
  }

  title (): string {
    return this.item.name
  }

  body (): string {
    const c = this.item.bookIds.length
    return `<span>${c} Books</span>`
  }

  to (): RawLocation {
    return { name: 'browse-readlist', params: { readListId: this.item.id.toString() } }
  }

  fabTo (): RawLocation {
    return undefined as unknown as RawLocation
  }
}
