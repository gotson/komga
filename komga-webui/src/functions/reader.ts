import KomgaBooksService from '@/services/komga-books.service'
import { BackgroundColors, ImageFit, ReadingDirection } from '@/types/enum-books'
import { Map } from '@/functions/forms'
import KomgaSeriesService from '@/services/komga-series.service'
import { getBookTitleCompact } from '@/functions/book-title'
import { Cookie, Mapper } from '@/functions/cookies'
import { VueCookies } from 'vue-cookies'
import { bookPageUrl } from '@/functions/urls'
import { checkWebpFeature } from '@/functions/check-webp'
import {
  DoubleSpreadBuilder,
  PageSpread,
  PageSpreadBuilder, PageSpreads,
  SingleSpreadBuilder,
  WebToonBuilder,
} from '@/functions/pages'
import { VueRouter } from 'vue-router/types/router'

export type Optional<V> = V | undefined

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
  private _navigator: Optional<Navigator> = undefined
  private _spreads: PageSpreads = {} as PageSpreads
  pageSpreadBuild: PageSpreadBuilder
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
    this.pageSpreadBuild = new SingleSpreadBuilder(this.$komgaBooks)

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
    this.addSetting(ReaderSettings.BG_COLOR, BackgroundColors.BLACK, IDENTITY_MAPPER)
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

    this.determinedSpreadBuilder()
    await this.buildSpread()
    this.loaded = true
  }

  /*
  @return true if builder changed
   */
  private determinedSpreadBuilder (): boolean {
    let newBuilder
    if (this.get(ReaderSettings.READ_DIR) === ReadingDirection.VERTICAL) {
      newBuilder = new SingleSpreadBuilder(this.$komgaBooks)
    } else if (this.get(ReaderSettings.DOUBLE_PAGES)) {
      newBuilder = new DoubleSpreadBuilder(this.$komgaBooks)
    } else {
      newBuilder = new SingleSpreadBuilder(this.$komgaBooks)
    }

    if (newBuilder.constructor !== this.pageSpreadBuild.constructor) {
      this.pageSpreadBuild = newBuilder
      return true
    }
    return false
  }

  public async buildSpread () {
    this._spreads = await this.pageSpreadBuild.getSpreads(this.book, this.pages)
    this._navigator = new Navigator(this, this._spreads)
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

  public bookTitle () {
    if (this.book?.metadata && this.series?.metadata) {
      return getBookTitleCompact(this.book.metadata.title, this.series.metadata.title)
    }
  }

  public get<V> (name: string): V {
    let c = this.settings[name] as Cookie<V>
    return c.get()
  }

  public async set<V> (name: string, value: V, save: boolean = true) {
    let c = this.settings[name] as Cookie<V>
    c.set(value, save)
    this.onSettingsChange()
  }

  private async onSettingsChange () {
    // Only rebuild spread if the builder changed
    if (this.determinedSpreadBuilder()) {
      await this.buildSpread()
    }
  }

  public determinePage (page: number): number {
    if (page >= 1 && page <= this.navigator?.count!!) {
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

  get spreads (): PageSpreads {
    return this._spreads
  }

  get navigator (): Optional<Navigator> {
    return this._navigator
  }

  markProgress (page: number) {
    if (page >= 1 && page <= this.pages.length) {
      this.$komgaBooks.updateReadProgress(this.book.id, { page: page })
    }
  }
}

export function readSetting<V> (setting: string): any {
  return {
    get: function (): V {
      return this.reader?.get(setting)
    },
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

export class Navigator {
  private _pageCursor: number
  private spreads : PageSpreads
  private reader: Reader

  constructor (reader: Reader, spreads: PageSpreads) {
    this._pageCursor = 1
    this.reader = reader
    this.spreads = spreads
  }

  get pageCursor (): number {
    return this._pageCursor
  }

  set pageCursor (value: number) {
    this._pageCursor = value
  }

  get count (): number {
    return this.spreads?.spreads?.length!!
  }

  get progress (): number {
    return (this.realPageNumber / this.realPageCount) * 100
  }

  get realPageNumber (): number {
    return this.spreads.spreads[this.pageCursor]?.pages[0]?.number
  }

  get realPageCount (): number {
    return this.reader.pages.length
  }

  goToFirst (): void {
    this.goTo(1)
  }

  goToLast (): void {
    this.goTo(this.count - 1)
  }

  goTo (page: number): void {
    this.pageCursor = this.reader?.spreads.indexOf.get(page)!!
  }

  forward (force: boolean = false) {
    if (!force && this.isVertical()) return
    return this.flipDirection() ? this.next() : this.prev()
  }

  backward (force: boolean = false) {
    if (!force && this.isVertical()) return
    return this.flipDirection() ? this.prev() : this.next()
  }

  up () {
    if (this.isVertical()) {
      this.prev()
    }
  }

  down () {
    if (this.isVertical()) {
      this.next()
    }
  }

  vtouch () {
    let swipe = this.swipe()
    return {
      left: () => { if (swipe) { this.forward() } },
      right: () => { if (swipe) { this.backward() } },
      up: () => { if (swipe) { this.down() } },
      down: () => { if (swipe) { this.up() } },
    }
  }

  private swipe (): boolean {
    return this.reader.get(ReaderSettings.SWIPE)
  }

  private flipDirection (): boolean {
    return this.reader.get(ReaderSettings.READ_DIR) === ReadingDirection.LEFT_TO_RIGHT
  }

  private isVertical (): boolean {
    return this.reader.get(ReaderSettings.READ_DIR) === ReadingDirection.VERTICAL
  }

  private hasNext () {
    return this._pageCursor < this.count
  }

  private hasPrev () {
    return this._pageCursor >= 1
  }

  private next () {
    if (this.hasNext()) {
      this._pageCursor++
    }
  }

  private prev () {
    if (this.hasPrev()) {
      this._pageCursor--
    }
  }

  goToBook ($router: VueRouter, sibling: BookDto) {
    $router.push({ name: 'read-book', params: { bookId: sibling.id.toString()!! } })
  }
}
