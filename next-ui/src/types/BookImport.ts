import type { BookDto, PageDto, SeriesDto, TransientBookDto } from '@/generated/openapi'
import { MediaStatus } from '@/types/MediaStatus'
import { defineMessage, type MessageDescriptor } from 'vue-intl'
import {
  type BookDetails,
  bookDtoToBookDetails,
  transientBookDtoToBookDetails,
} from '@/types/BookDetails'

export class BookImport {
  transientBook: TransientBookDto
  destinationName: string
  originalName: string
  series?: SeriesDto
  seriesBooks?: BookDto[]
  upgradeBook?: BookDto
  upgradeBookPages: PageDto[]
  imported: boolean

  constructor(transientBook: TransientBookDto) {
    this.transientBook = transientBook
    this.originalName = transientBook.name
    this.destinationName = transientBook.name
    this.upgradeBookPages = []
    this.imported = false
  }

  /**
   * Whether the book is selectable.
   * Only books in READY status and not yet imported can be selected
   */
  public get selectable(): boolean {
    return this.transientBook.status === MediaStatus.Ready && !this.imported
  }

  public get upgradable(): boolean {
    return this.selectable && !!this.series && !!this.seriesBooks
  }

  public get importable(): boolean {
    return this.selectable && !!this.series
  }

  public get statusMessage(): MessageDescriptor | undefined {
    switch (this.transientBook.status) {
      case MediaStatus.Unknown:
        return defineMessage({
          description: 'Import books: status message: book needs to be analyzed first',
          defaultMessage: 'Book needs to be analyzed first',
          id: 'CPMLrI',
        })
      case MediaStatus.Unsupported:
        return defineMessage({
          description: 'Import books: status message: book format is not supported',
          defaultMessage: 'Book format is not supported',
          id: 'g2UW+6',
        })
      case MediaStatus.Error:
        return defineMessage({
          description: 'Import books: status message: book could not be analyzed',
          defaultMessage: 'Book could not be analyzed',
          id: '8jE3eP',
        })
    }
    if (!this.series)
      return defineMessage({
        description: 'Import books: status message: choose a series',
        defaultMessage: 'Choose a series',
        id: 'cM9FuW',
      })
    if (this.imported)
      return defineMessage({
        description: 'Import books: status message: import requested',
        defaultMessage: 'Import requested',
        id: 'YHxouG',
      })
    if (this.upgradeBook)
      return defineMessage({
        description: 'Import books: status message: book will be upgraded',
        defaultMessage: 'Book will be upgraded',
        id: 'UoaxO7',
      })
  }

  public get transientBookDetails(): BookDetails {
    return transientBookDtoToBookDetails(this.transientBook)
  }

  public get upgradeBookDetails(): BookDetails | undefined {
    if (this.upgradeBook) return bookDtoToBookDetails(this.upgradeBook, this.upgradeBookPages)
  }
}
