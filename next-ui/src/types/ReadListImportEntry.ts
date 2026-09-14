import { defineMessage, type MessageDescriptor } from 'vue-intl'
import type {
  BookDto,
  ReadListRequestBookMatchBookDto,
  ReadListRequestBookMatchesDto,
  ReadListRequestBookMatchSeriesDto,
} from '@/generated/openapi'

export class ReadListImportEntry {
  index: number
  request: ReadListRequestBookMatchesDto
  series?: ReadListRequestBookMatchSeriesDto
  book?: ReadListRequestBookMatchBookDto
  selectableFn: () => boolean
  shouldFetchSeriesBooks: boolean
  seriesBooks?: BookDto[]

  constructor(request: ReadListRequestBookMatchesDto, index: number, selectableFn: () => boolean) {
    this.index = index
    this.request = request
    const match = request.matches.find(Boolean)
    if (match) {
      this.series = match.series
      this.book = match.books.find(Boolean)
    }
    this.selectableFn = selectableFn
    this.shouldFetchSeriesBooks = false
  }

  public get selectable(): boolean {
    return this.selectableFn()
  }

  public get importable(): boolean {
    return !!this.book
  }

  public get statusMessage(): MessageDescriptor | undefined {
    if (!this.series)
      return defineMessage({
        description: 'Import reading list: status message: choose a series',
        defaultMessage: 'Choose a series',
        id: 'H2B6uF',
      })
    if (!this.book)
      return defineMessage({
        description: 'Import reading list: status message: choose a book',
        defaultMessage: 'Choose a book',
        id: 'xYp/8u',
      })
  }
}
