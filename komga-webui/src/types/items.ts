import {bookThumbnailUrl, collectionThumbnailUrl, readListThumbnailUrl, seriesThumbnailUrl} from '@/functions/urls'
import {RawLocation} from 'vue-router/types/router'
import {BookDto} from '@/types/komga-books'
import {SeriesDto} from '@/types/komga-series'
import i18n from '@/i18n'
import {MediaStatus} from '@/types/enum-books'
import {getFileSize} from '@/functions/file'

export enum ItemTypes {
  BOOK, SERIES, COLLECTION, READLIST
}

export enum ItemContext {
  RELEASE_DATE = 'RELEASE_DATE',
  DATE_ADDED = 'DATE_ADDED',
  DATE_UPDATED = 'DATE_UPDATED',
  FILE_SIZE = 'FILE_SIZE',
  SHOW_SERIES = 'SHOW_SERIES',
  READ_DATE = 'READ_DATE',
}

export interface ItemTitle {
  title: string,
  to: RawLocation,
}

export function createItem(item: BookDto | SeriesDto | CollectionDto | ReadListDto): Item<BookDto | SeriesDto | CollectionDto | ReadListDto> {
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

  constructor(item: T) {
    this.item = item
  }

  subtitleProps(): Object {
    return {
      style: 'word-break: normal !important; height: 4em',
      'class': 'pa-2 pb-1 text--primary',
    }
  }

  abstract type(): ItemTypes

  abstract thumbnailUrl(): string

  abstract title(context: ItemContext[]): ItemTitle | ItemTitle[]

  abstract body(context: ItemContext[]): string

  abstract to(): RawLocation

  abstract fabTo(): RawLocation
}

export class BookItem extends Item<BookDto> {
  thumbnailUrl(): string {
    return bookThumbnailUrl(this.item.id)
  }

  type(): ItemTypes {
    return ItemTypes.BOOK
  }

  title(context: ItemContext[]): ItemTitle | ItemTitle[] {
    if (context.includes(ItemContext.SHOW_SERIES))
      return [
        {
          title: `${this.item.seriesTitle}`,
          to: this.seriesTo(),
        },
        {
          title: `${this.item.metadata.number} - ${this.item.metadata.title}`,
          to: this.to(),
        },
      ]
    return {
      title: `${this.item.metadata.number} - ${this.item.metadata.title}`,
      to: this.to(),
    }
  }

  body(context: ItemContext[] = []): string {
    if (this.item.deleted) return `<div class="text-truncate error--text">${i18n.t('common.unavailable')}</div>`
    switch (this.item.media.status) {
      case MediaStatus.ERROR:
        return `<div class="text-truncate error--text">${i18n.t('book_card.error')}</div>`
      case MediaStatus.UNSUPPORTED:
        return `<div class="text-truncate warning--text">${i18n.t('book_card.unsupported')}</div>`
      case MediaStatus.UNKNOWN:
        return `<div class="text-truncate">${i18n.t('book_card.unknown')}</div>`
      default:
        let text
        if (context.includes(ItemContext.RELEASE_DATE))
          text = this.item.metadata.releaseDate ? new Intl.DateTimeFormat(i18n.locale, {dateStyle: 'medium', timeZone: 'UTC'} as Intl.DateTimeFormatOptions).format(new Date(this.item.metadata.releaseDate)) : i18n.t('book_card.no_release_date')
        else if (context.includes(ItemContext.DATE_ADDED))
          text = new Intl.DateTimeFormat(i18n.locale, {dateStyle: 'medium'} as Intl.DateTimeFormatOptions).format(new Date(this.item.created))
        else if (context.includes(ItemContext.READ_DATE))
          text = this.item.readProgress?.lastModified ? new Intl.DateTimeFormat(i18n.locale, {dateStyle: 'medium'} as Intl.DateTimeFormatOptions).format(new Date(this.item.readProgress?.lastModified)) : i18n.t('book_card.unread')
        else if (context.includes(ItemContext.FILE_SIZE))
          text = getFileSize(this.item.sizeBytes)
        else
          text = i18n.tc('common.pages_n', this.item.media.pagesCount)
        return `<div class="text-truncate">${text}</div>`
    }
  }

  to(): RawLocation {
    return {
      name: 'browse-book',
      params: {bookId: this.item.id},
      query: {context: this.item?.context?.origin, contextId: this.item?.context?.id},
    }
  }

  seriesTo(): RawLocation {
    return {
      name: 'browse-series',
      params: {seriesId: this.item.seriesId},
    }
  }

  fabTo(): RawLocation {
    return {
      name: 'read-book',
      params: {bookId: this.item.id},
      query: {context: this.item?.context?.origin, contextId: this.item?.context?.id},
    }
  }
}

export class SeriesItem extends Item<SeriesDto> {
  thumbnailUrl(): string {
    return seriesThumbnailUrl(this.item.id)
  }

  type(): ItemTypes {
    return ItemTypes.SERIES
  }

  title(context: ItemContext[]): ItemTitle | ItemTitle[] {
    return {
      title: this.item.metadata.title,
      to: this.to(),
    }
  }

  body(context: ItemContext[] = []): string {
    if (this.item.deleted) return `<div class="text-truncate error--text">${i18n.t('common.unavailable')}</div>`

    let text
    if (context.includes(ItemContext.RELEASE_DATE))
      text = this.item.booksMetadata.releaseDate ? new Intl.DateTimeFormat(i18n.locale, {dateStyle: 'medium', timeZone: 'UTC'} as Intl.DateTimeFormatOptions).format(new Date(this.item.booksMetadata.releaseDate)) : i18n.t('book_card.no_release_date')
    else if (context.includes(ItemContext.DATE_ADDED))
      text = new Intl.DateTimeFormat(i18n.locale, {dateStyle: 'medium'} as Intl.DateTimeFormatOptions).format(new Date(this.item.created))
    else if (context.includes(ItemContext.DATE_UPDATED))
      text = new Intl.DateTimeFormat(i18n.locale, {dateStyle: 'medium'} as Intl.DateTimeFormatOptions).format(new Date(this.item.lastModified))
    else
      text = i18n.tc('common.books_n', this.item.booksCount)
    return `<div class="text-truncate">${text}</div>`
  }

  to(): RawLocation {
    return {name: 'browse-series', params: {seriesId: this.item.id.toString()}}
  }

  fabTo(): RawLocation {
    return undefined as unknown as RawLocation
  }
}

export class CollectionItem extends Item<CollectionDto> {
  thumbnailUrl(): string {
    return collectionThumbnailUrl(this.item.id)
  }

  type(): ItemTypes {
    return ItemTypes.COLLECTION
  }

  title(context: ItemContext[]): ItemTitle | ItemTitle[] {
    return {
      title: this.item.name,
      to: this.to(),
    }
  }

  body(context: ItemContext[] = []): string {
    const c = this.item.seriesIds.length
    return `<span>${c} Series</span>`
  }

  to(): RawLocation {
    return {name: 'browse-collection', params: {collectionId: this.item.id.toString()}}
  }

  fabTo(): RawLocation {
    return undefined as unknown as RawLocation
  }
}

export class ReadListItem extends Item<ReadListDto> {
  thumbnailUrl(): string {
    return readListThumbnailUrl(this.item.id)
  }

  type(): ItemTypes {
    return ItemTypes.READLIST
  }

  title(context: ItemContext[]): ItemTitle | ItemTitle[] {
    return {
      title: this.item.name,
      to: this.to(),
    }
  }

  body(context: ItemContext[] = []): string {
    const c = this.item.bookIds.length
    return `<span>${c} Books</span>`
  }

  to(): RawLocation {
    return {name: 'browse-readlist', params: {readListId: this.item.id.toString()}}
  }

  fabTo(): RawLocation {
    return undefined as unknown as RawLocation
  }
}
