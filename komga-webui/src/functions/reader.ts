import KomgaBooksService from '@/services/komga-books.service'
import { ImageFit, ReadingDirection } from '@/types/enum-books'
import { Map } from '@/functions/forms'
import KomgaSeriesService from '@/services/komga-series.service'
import { getBookTitleCompact } from '@/functions/book-title'
import { Cookie, Mapper } from '@/functions/cookies'
import { VueCookies } from 'vue-cookies'
import { bookPageUrl } from '@/functions/urls'
import { checkWebpFeature } from '@/functions/check-webp'

type Optional<V> = V | undefined

export enum ReaderSettings {
  DOUBLE_PAGES = 'webreader.doublePages',
  SWIPE = 'webreader.swipe',
  FIT = 'webreader.fit',
  READ_DIR = 'webreader.readingDirection',
  ANIMATIONS = 'webreader.animations',
  BG_COLOR = 'webreader.background',
}

const TRUE_MAPPER = (v: any) => v === 'true'
const IDENTITY_MAPPER = (v: any) => v

async function tryCatch<V> (f: () => Promise<V>, defaultValue: V): Promise<V> {
  return f().catch((r) => {
    return defaultValue
  })
}

export class Reader {
  $komgaBooks: KomgaBooksService
  $komgaSeries: KomgaSeriesService
  $cookie: VueCookies
  readonly settings: Map<Cookie<any>> = {}
  public siblingPrev: Optional<BookDto>
  public siblingNext: Optional<BookDto>
  public book: BookDto = {} as BookDto
  public series: SeriesDto = {} as SeriesDto
  public pages: PageDto[] = []
  bookId: string = ''
  loaded: boolean = false

  convertTo: string = 'jpeg'
  SUPPORTED_MEDIA_TYPES = ['image/jpeg', 'image/png', 'image/gif']

  constructor ($komgaBooks: KomgaBooksService, $komgaSeries: KomgaSeriesService, $cookie: VueCookies) {
    // services
    this.$komgaBooks = $komgaBooks
    this.$komgaSeries = $komgaSeries
    this.$cookie = $cookie
    this.defaultSettings()

    checkWebpFeature('lossy', (feature, isSupported) => {
      if (isSupported) {
        this.SUPPORTED_MEDIA_TYPES.push('image/webp')
      }
    })
  }

  private addSetting<V> (name: string, defaultValue: V, mapper: Mapper<V>) {
    this.settings[name] = new Cookie<V>(name, defaultValue, mapper, this.$cookie)
  }

  defaultSettings () {
    this.addSetting(ReaderSettings.DOUBLE_PAGES, false, TRUE_MAPPER)
    this.addSetting(ReaderSettings.SWIPE, true, TRUE_MAPPER)
    this.addSetting(ReaderSettings.ANIMATIONS, true, TRUE_MAPPER)
    this.addSetting(ReaderSettings.FIT, ImageFit.HEIGHT, IDENTITY_MAPPER)
    this.addSetting(ReaderSettings.READ_DIR, ReadingDirection.LEFT_TO_RIGHT, IDENTITY_MAPPER)
    this.addSetting(ReaderSettings.BG_COLOR, 'black', IDENTITY_MAPPER)
  }

  public async setup (bookId: string) {
    const $komgaBooks = this.$komgaBooks
    this.bookId = bookId
    this.book = await $komgaBooks.getBook(bookId)
    this.pages = await $komgaBooks.getBookPages(bookId)
    this.series = await this.$komgaSeries.getOneSeries(this.book.seriesId)
    this.siblingPrev = await tryCatch(() => $komgaBooks.getBookSiblingPrevious(bookId), undefined)
    this.siblingNext = await tryCatch(() => $komgaBooks.getBookSiblingNext(bookId), undefined)
    // Load all settings from cookies
    for (let setting of Object.values(this.settings)) {
      setting.load()
    }

    this.loaded = true
  }

  /*
  @return true if reading direction was coerced by book metadata
   */
  public coerceReadingDirection (): boolean {
    // set non-persistent reading direction if exists in metadata
    let metadataReadingDirection = this.book.metadata.readingDirection
    if (metadataReadingDirection) {
      let readingDirection = this.get(ReaderSettings.READ_DIR)
      if (readingDirection !== metadataReadingDirection) {
        this.set(ReaderSettings.READ_DIR, metadataReadingDirection, false)
        return true
      }
    }
    return false
  }

  public pagesCount () {
    return this.pages.length
  }

  public bookTitle () {
    if (this.book?.metadata && this.series?.metadata) {
      return getBookTitleCompact(this.book.metadata.title, this.series.metadata.title)
    }
  }

  public get<V> (name: string): V {
    let c = this.settings[name] as Cookie<V>
    return c.get()
  }

  public set<V> (name: string, value: V, save: boolean = true) {
    let c = this.settings[name] as Cookie<V>
    c.set(value, save)
  }

  public getPageUrl (page: number): string {
    if (!this.SUPPORTED_MEDIA_TYPES.includes(this.pages[page - 1].mediaType)) {
      return bookPageUrl(this.bookId, page, this.convertTo)
    } else {
      return bookPageUrl(this.bookId, page)
    }
  }

  public determinePage (page: number): number {
    if (page >= 1 && page <= this.pagesCount()) {
      return page
    } else if (!this.isCompleted()) {
      return this.book.readProgress?.page!!
    } else {
      return 1
    }
  }

  public isCompleted (): Optional<boolean> {
    return this.book.readProgress?.completed
  }
}

export function computedSetting<V> (setting: string, save: boolean = true): any {
  return {
    get: function (): V {
      return this.reader?.get(setting)
    },
    set: function (value: V) {
      return this.reader?.set(setting, value, save)
    },
  }
}
