<template>
  <v-container class="ma-0 pa-0 full-height" fluid v-if="pages.length > 0"
               :style="`width: 100%; background-color: ${backgroundColor}`"
               v-touch="{
                 left: () => {if(swipe) {turnRight()}},
                 right: () => {if(swipe) {turnLeft()}},
                 up: () => {if(swipe) {verticalNext()}},
                 down: () => {if(swipe) {verticalPrev()}}
                 }"
  >
    <div>
      <v-slide-y-transition>
        <!-- Top Toolbar-->
        <v-toolbar
          dense elevation="1"
          v-if="toolbar"
          class="settings full-width"
          style="position: fixed; top: 0"
        >
          <v-btn
            icon
            @click="closeBook"
          >
            <v-icon>mdi-arrow-left</v-icon>
          </v-btn>
          <v-toolbar-title> {{ bookTitle }}</v-toolbar-title>
          <v-spacer></v-spacer>
          <shortcut-help-menu/>
          <v-btn
            icon
            @click="showThumbnailsExplorer = !showThumbnailsExplorer"
          >
            <v-icon>mdi-view-grid</v-icon>
          </v-btn>
          <v-btn
            icon
            @click="menu = !menu"
          >
            <v-icon>mdi-settings</v-icon>
          </v-btn>
        </v-toolbar>
      </v-slide-y-transition>
      <v-slide-y-reverse-transition>
        <!-- Bottom Toolbar-->
        <v-toolbar
          dense
          elevation="1"
          class="settings full-width"
          style="position: fixed; bottom: 0"
          horizontal
          v-if="toolbar"
        >
          <v-row justify="center">
            <!--  Menu: page slider  -->
            <v-col class="px-0">
              <v-slider
                hide-details
                thumb-label
                @change="goTo"
                v-model="goToPage"
                class="align-center"
                min="1"
                :max="pagesCount"
              >
                <template v-slot:prepend>
                  <v-icon @click="previousBook" class="">mdi-undo</v-icon>
                  <v-icon @click="goToFirst" class="mx-2">mdi-skip-previous</v-icon>
                  <v-label>
                    {{ currentPage }}
                  </v-label>
                </template>
                <template v-slot:append>
                  <v-label>
                    {{ pagesCount }}
                  </v-label>
                  <v-icon @click="goToLast" class="mx-1">mdi-skip-next</v-icon>
                  <v-icon @click="nextBook" class="">mdi-redo</v-icon>
                </template>
              </v-slider>
            </v-col>
          </v-row>

        </v-toolbar>
      </v-slide-y-reverse-transition>
    </div>

    <!--  clickable zone: left  -->
    <div @click="turnLeft()"
         class="left-quarter full-height top"
         style="z-index: 1;"
    />

    <!--  clickable zone: menu  -->
    <div @click="toolbar = !toolbar"
         class="center-half full-height top"
         style="z-index: 1;"
    />

    <!--  clickable zone: right  -->
    <div @click="turnRight()"
         class="right-quarter full-height top"
         style="z-index: 1;"
    />

    <div class="full-height">
      <!--  Carousel  -->
      <v-carousel v-model="carouselPage"
                  :show-arrows="false"
                  :continuous="false"
                  :reverse="flipDirection"
                  :vertical="vertical"
                  hide-delimiters
                  touchless
                  height="100%"
      >
        <!--  Carousel: pages  -->
        <v-carousel-item v-for="(spread, i) in spreads"
                         :key="`spread${i}`"
                         :eager="eagerLoad(i)"
                         class="full-height"
                         :transition="animations ? undefined : false"
                         :reverse-transition="animations ? undefined : false"
        >
          <div class="full-height d-flex flex-column justify-center">
            <div :class="`d-flex flex-row${flipDirection ? '-reverse' : ''} justify-center px-0 mx-0` ">
              <img v-for="(page, j) in spread"
                   :key="`spread${i}-${j}`"
                   :src="getPageUrl(page.number)"
                   :height="maxHeight"
                   :width="maxWidth(i)"
                   :style="imgStyle"
              />
            </div>
          </div>
        </v-carousel-item>
      </v-carousel>
    </div>

    <thumbnail-explorer-dialog
      v-model="showThumbnailsExplorer"
      :bookId="bookId"
      @goToPage="goTo"
      :pagesCount="pagesCount"
    ></thumbnail-explorer-dialog>

    <v-bottom-sheet
      v-model="menu"
      :close-on-content-click="false"
      :width="$vuetify.breakpoint.width * ($vuetify.breakpoint.smAndUp ? 0.5 : 1)"
      @keydown.esc.stop=""
    >
      <v-container fluid class="pa-0">
        <v-toolbar dark color="primary">
          <v-btn icon dark @click="menu = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
          <v-toolbar-title>Reader Settings</v-toolbar-title>
        </v-toolbar>

        <v-list class="full-height full-width">
          <v-list-item>
            <settings-switch v-model="doublePages" label="Page Layout"
                             :status="`${ doublePages ? 'Double Pages' : 'Single Page'}`"></settings-switch>
          </v-list-item>

          <v-list-item>
            <settings-switch v-model="animations" label="Page Transitions"></settings-switch>
          </v-list-item>

          <v-list-item>
            <settings-switch v-model="swipe" label="Swipe navigation"></settings-switch>
          </v-list-item>

          <v-list-item>
            <settings-select
              :items="backgroundColors"
              v-model="backgroundColor"
              label="Background color"
            >
            </settings-select>
          </v-list-item>

          <v-list-item>
            <settings-select
              :items="readingDirs"
              v-model="readingDirection"
              label="Reading Direction"
            />
          </v-list-item>

          <v-list-item>
            <settings-select
              :items="imageFits"
              v-model="imageFit"
              label="Scaling"
            />
          </v-list-item>
        </v-list>
      </v-container>
    </v-bottom-sheet>
    <v-snackbar
      v-model="jumpToPreviousBook"
      :timeout="jumpConfirmationDelay"
      vertical
      top
      color="rgba(0, 0, 0, 0.7)"
      multi-line
      class="mt-12"
    >
      <div class="title pa-6">
        <p>You're at the beginning<br/>of the book.</p>
        <p v-if="!$_.isEmpty(siblingPrevious)">Click or press previous again<br/>to move to the previous book.</p>
      </div>
    </v-snackbar>

    <v-snackbar
      v-model="jumpToNextBook"
      :timeout="jumpConfirmationDelay"
      vertical
      top
      color="rgba(0, 0, 0, 0.7)"
      multi-line
      class="mt-12"
    >
      <div class="title pa-6">
        <p>You've reached the end<br/>of the book.</p>
        <p v-if="!$_.isEmpty(siblingNext)">Click or press next again<br/>to move to the next book.</p>
        <p v-else>Click or press next again<br/>to exit the reader.</p>
      </div>
    </v-snackbar>

    <v-snackbar v-model="snackReadingDirection"
                color="info"
                multi-line
                vertical
                top
    >
      <div>This book has specific reading direction set.</div>
      <div>Reading direction has been set to <span
        class="font-weight-bold">{{ readingDirectionText(readingDirection as ReadingDirection) }}</span>.
      </div>
      <div>This only applies to this book and will not overwrite your settings.</div>
      <v-btn
        text
        @click="snackReadingDirection = false"
      >
        Dismiss
      </v-btn>
    </v-snackbar>

    <v-snackbar
      v-model="notification.enabled"
      class="mb-12"
      color="rgba(0, 0, 0, 0.8)"
      multi-line
      vertical
      centered
      :timeout="notification.timeout"
    >
      <div class="title pa-2">
        {{ notification.message }}
      </div>
    </v-snackbar>
  </v-container>
</template>

<script lang="ts">
import SettingsSelect from '@/components/SettingsSelect.vue'
import SettingsSwitch from '@/components/SettingsSwitch.vue'
import ThumbnailExplorerDialog from '@/components/dialogs/ThumbnailExplorerDialog.vue'
import ShortcutHelpMenu from '@/components/menus/ShortcutHelpMenu.vue'
import { getBookTitleCompact } from '@/functions/book-title'
import { checkWebpFeature } from '@/functions/check-webp'
import { bookPageUrl } from '@/functions/urls'
import { ReadingDirection } from '@/types/enum-books'
import { executeShortcut } from '@/functions/shortcuts'
import Vue from 'vue'
import { isPageLandscape } from '@/functions/page'

const cookieFit = 'webreader.fit'
const cookieReadingDirection = 'webreader.readingDirection'
const cookieDoublePages = 'webreader.doublePages'
const cookieSwipe = 'webreader.swipe'
const cookieAnimations = 'webreader.animations'
const cookieBackground = 'webreader.background'

enum ImageFit {
  WIDTH = 'width',
  HEIGHT = 'height',
  ORIGINAL = 'original'
}

export default Vue.extend({
  name: 'BookReader',
  components: { SettingsSwitch, SettingsSelect, ThumbnailExplorerDialog, ShortcutHelpMenu },
  data: () => {
    return {
      ImageFit,
      book: {} as BookDto,
      series: {} as SeriesDto,
      siblingPrevious: {} as BookDto,
      siblingNext: {} as BookDto,
      jumpToNextBook: false,
      jumpToPreviousBook: false,
      jumpConfirmationDelay: 3000,
      snackReadingDirection: false,
      pages: [] as PageDto[],
      supportedMediaTypes: ['image/jpeg', 'image/png', 'image/gif'],
      convertTo: 'jpeg',
      carouselPage: 0,
      showThumbnailsExplorer: false,
      toolbar: false,
      menu: false,
      dialogGoto: false,
      goToPage: 1,
      settings: {
        doublePages: false,
        swipe: true,
        fit: ImageFit.HEIGHT,
        readingDirection: ReadingDirection.LEFT_TO_RIGHT,
        animations: true,
        backgroundColor: 'black',
        imageFits: Object.values(ImageFit),
        readingDirs: Object.values(ReadingDirection),
      },
      notification: {
        enabled: false,
        message: '',
        timeout: 4000,
      },
      readingDirs: [
        { text: 'Left to right', value: ReadingDirection.LEFT_TO_RIGHT },
        { text: 'Right to left', value: ReadingDirection.RIGHT_TO_LEFT },
        { text: 'Vertical', value: ReadingDirection.VERTICAL },
      ],
      imageFits: [
        { text: 'Fit to height', value: ImageFit.HEIGHT },
        { text: 'Fit to width', value: ImageFit.WIDTH },
        { text: 'Original', value: ImageFit.ORIGINAL },
      ],
      backgroundColors: [
        { text: 'White', value: 'white' },
        { text: 'Black', value: 'black' },
      ],
    }
  },
  created () {
    checkWebpFeature('lossy', (feature, isSupported) => {
      if (isSupported) {
        this.supportedMediaTypes.push('image/webp')
      }
    })
  },
  async mounted () {
    window.addEventListener('keydown', this.keyPressed)

    this.loadFromCookie(cookieReadingDirection, (v) => {
      this.readingDirection = v
    })
    this.loadFromCookie(cookieAnimations, (v) => {
      this.animations = (v === 'true')
    })
    this.loadFromCookie(cookieDoublePages, (v) => {
      this.doublePages = (v === 'true')
    })
    this.loadFromCookie(cookieSwipe, (v) => {
      this.swipe = (v === 'true')
    })
    this.loadFromCookie(cookieFit, (v) => {
      if (v) {
        this.imageFit = v
      }
    })
    this.loadFromCookie(cookieBackground, (v) => {
      if (v) {
        this.backgroundColor = v
      }
    })

    this.setup(this.bookId, Number(this.$route.query.page))
  },
  destroyed () {
    window.removeEventListener('keydown', this.keyPressed)
  },
  props: {
    bookId: {
      type: String,
      required: true,
    },
  },
  async beforeRouteUpdate (to, from, next) {
    if (to.params.bookId !== from.params.bookId) {
      // route update means going to previous/next book, in this case we start from first page
      this.setup(to.params.bookId, 1)
    }
    next()
  },
  watch: {
    currentPage (val) {
      this.updateRoute()
      this.goToPage = val
      this.markProgress(val)
    },
    async book (val) {
      if (this.$_.has(val, 'name')) {
        this.series = await this.$komgaSeries.getOneSeries(val.seriesId)
        document.title = `Komga - ${getBookTitleCompact(val.metadata.title, this.series.metadata.title)}`
      }
    },
  },
  computed: {
    currentSlide (): number {
      return this.carouselPage + 1
    },
    currentPage (): number {
      if (this.carouselPage >= 0 && this.carouselPage < this.spreads.length && this.spreads.length > 0) {
        return this.spreads[this.carouselPage][0].number
      }
      return 1
    },
    canPrev (): boolean {
      return this.currentSlide > 1
    },
    canNext (): boolean {
      return this.currentSlide < this.slidesCount
    },
    progress (): number {
      return this.currentPage / this.pagesCount * 100
    },
    maxHeight (): number | null {
      return this.imageFit === ImageFit.HEIGHT ? this.$vuetify.breakpoint.height : null
    },
    imgStyle (): string {
      return this.imageFit === ImageFit.WIDTH ? 'height:intrinsic' : ''
    },
    slidesCount (): number {
      return this.spreads.length
    },
    pagesCount (): number {
      return this.pages.length
    },
    bookTitle (): string {
      return getBookTitleCompact(this.book.metadata.title, this.series.metadata.title)
    },

    animations: {
      get: function (): boolean {
        return this.settings.animations
      },
      set: function (animations: boolean): void {
        this.settings.animations = animations
        this.$cookies.set(cookieAnimations, animations, Infinity)
      },
    },
    readingDirection: {
      get: function (): ReadingDirection {
        return this.settings.readingDirection
      },
      set: function (readingDirection: ReadingDirection): void {
        this.settings.readingDirection = readingDirection
        this.$cookies.set(cookieReadingDirection, readingDirection, Infinity)
      },
    },
    flipDirection (): boolean {
      return this.readingDirection === ReadingDirection.RIGHT_TO_LEFT
    },
    vertical (): boolean {
      return this.readingDirection === ReadingDirection.VERTICAL
    },
    imageFit: {
      get: function (): ImageFit {
        return this.settings.fit
      },
      set: function (fit: ImageFit): void {
        this.settings.fit = fit
        this.$cookies.set(cookieFit, fit, Infinity)
      },
    },
    backgroundColor: {
      get: function (): string {
        return this.settings.backgroundColor
      },
      set: function (color: string): void {
        this.settings.backgroundColor = color
        this.$cookies.set(cookieBackground, color, Infinity)
      },
    },
    doublePages: {
      get: function (): boolean {
        return this.settings.doublePages
      },
      set: function (doublePages: boolean): void {
        const current = this.currentPage
        this.settings.doublePages = doublePages
        this.goTo(current)
        this.$cookies.set(cookieDoublePages, doublePages, Infinity)
      },
    },
    swipe: {
      get: function (): boolean {
        return this.settings.swipe
      },
      set: function (swipe: boolean): void {
        this.settings.swipe = swipe
        this.$cookies.set(cookieSwipe, swipe, Infinity)
      },
    },
    spreads (): PageDto[][] {
      if (this.pages.length === 0) return []
      if (this.doublePages) {
        const spreads = []
        spreads.push([this.pages[0]])
        const pages = this.$_.drop(this.$_.dropRight(this.pages)) as PageDto[]
        while (pages.length > 0) {
          const p = pages.shift() as PageDto
          if (isPageLandscape(p)) {
            spreads.push([p])
          } else {
            if (pages.length > 0) {
              const p2 = pages.shift() as PageDto
              if (isPageLandscape(p2)) {
                spreads.push([p])
                spreads.push([p2])
              } else {
                spreads.push([p, p2])
              }
            }
          }
        }
        spreads.push([this.pages[this.pages.length - 1]])
        return spreads
      } else {
        return this.pages.map(p => [p])
      }
    },
  },
  methods: {
    keyPressed (e: KeyboardEvent) {
      executeShortcut(this, e)
    },
    async setup (bookId: string, page: number) {
      this.book = await this.$komgaBooks.getBook(bookId)
      this.pages = await this.$komgaBooks.getBookPages(bookId)
      if (page >= 1 && page <= this.pagesCount) {
        this.goTo(page)
      } else if (this.book.readProgress?.completed === false) {
        this.goTo(this.book.readProgress?.page!!)
      } else {
        this.goToFirst()
      }

      // set non-persistent reading direction if exists in metadata
      switch (this.book.metadata.readingDirection) {
        case ReadingDirection.LEFT_TO_RIGHT:
        case ReadingDirection.RIGHT_TO_LEFT:
        case ReadingDirection.VERTICAL:
          if (this.readingDirection !== this.book.metadata.readingDirection) {
            // bypass setter so cookies aren't set
            this.settings.readingDirection = this.book.metadata.readingDirection
            this.snackReadingDirection = true
          }
          break
      }

      try {
        this.siblingNext = await this.$komgaBooks.getBookSiblingNext(bookId)
      } catch (e) {
        this.siblingNext = {} as BookDto
      }
      try {
        this.siblingPrevious = await this.$komgaBooks.getBookSiblingPrevious(bookId)
      } catch (e) {
        this.siblingPrevious = {} as BookDto
      }
    },
    readingDirectionText (readingDirection: ReadingDirection): string {
      return readingDirs.find(x => x.value === readingDirection).text
    },
    getPageUrl (page: number): string {
      if (!this.supportedMediaTypes.includes(this.pages[page - 1].mediaType)) {
        return bookPageUrl(this.bookId, page, this.convertTo)
      } else {
        return bookPageUrl(this.bookId, page)
      }
    },
    turnRight () {
      if (this.vertical) return
      return this.flipDirection ? this.prev() : this.next()
    },
    turnLeft () {
      if (this.vertical) return
      return this.flipDirection ? this.next() : this.prev()
    },
    verticalPrev () {
      if (this.vertical) this.prev()
    },
    verticalNext () {
      if (this.vertical) this.next()
    },
    prev () {
      if (this.canPrev) {
        this.carouselPage--
        window.scrollTo(0, 0)
      } else {
        if (this.jumpToPreviousBook) {
          this.previousBook()
        } else {
          this.jumpToPreviousBook = true
        }
      }
    },
    next () {
      if (this.canNext) {
        this.carouselPage++
        window.scrollTo(0, 0)
      } else {
        if (this.jumpToNextBook) {
          this.nextBook()
        } else {
          this.jumpToNextBook = true
        }
      }
    },
    nextBook () {
      if (this.$_.isEmpty(this.siblingNext)) {
        this.closeBook()
      } else {
        this.jumpToNextBook = false
        this.$router.push({ name: 'read-book', params: { bookId: this.siblingNext.id.toString() } })
      }
    },
    previousBook () {
      if (!this.$_.isEmpty(this.siblingPrevious)) {
        this.jumpToPreviousBook = false
        this.$router.push({ name: 'read-book', params: { bookId: this.siblingPrevious.id.toString() } })
      }
    },
    goTo (page: number) {
      this.carouselPage = this.toSpreadIndex(page)
    },
    goToFirst () {
      this.goTo(1)
    },
    goToLast () {
      this.goTo(this.pagesCount)
    },
    updateRoute () {
      this.$router.replace({
        name: this.$route.name,
        params: { bookId: this.$route.params.bookId },
        query: {
          page: this.currentPage.toString(),
        },
      })
    },
    closeBook () {
      this.$router.push({ name: 'browse-book', params: { bookId: this.bookId.toString() } })
    },
    toSinglePages (i: number): number {
      if (i === 1) return 1
      if (i === this.slidesCount) return this.pagesCount
      return (i - 1) * 2
    },
    toSpreadIndex (i: number): number {
      if (this.spreads.length > 0) {
        if (this.doublePages) {
          for (let j = 0; j < this.spreads.length; j++) {
            for (let k = 0; k < this.spreads[j].length; k++) {
              if (this.spreads[j][k].number === i) {
                return j
              }
            }
          }
        } else {
          return i - 1
        }
      }
      return i - 1
    },
    eagerLoad (spreadIndex: number): boolean {
      return Math.abs(this.carouselPage - spreadIndex) <= 2
    },
    maxWidth (spreadIndex: number): number | null {
      if (this.imageFit !== ImageFit.WIDTH) {
        return null
      }
      if (this.doublePages && this.spreads[spreadIndex].length === 2) {
        return this.$vuetify.breakpoint.width / 2
      }
      return this.$vuetify.breakpoint.width
    },
    loadFromCookie (cookieKey: string, setter: (value: any) => void): void {
      if (this.$cookies.isKey(cookieKey)) {
        setter(this.$cookies.get(cookieKey))
      }
    },
    changeReadingDir (dir: ReadingDirection) {
      this.readingDirection = dir
      const i = this.settings.readingDirs.indexOf(this.readingDirection)
      const text = this.readingDirs[i].text
      this.sendNotification(`Changing Reading Direction to: ${text}`)
    },
    cycleScale () {
      const fit: ImageFit = this.settings.fit
      const i = (this.settings.imageFits.indexOf(fit) + 1) % (this.settings.imageFits.length)
      this.imageFit = this.settings.imageFits[i]
      const text = this.imageFits[i].text
      // The text here only works cause this.imageFits has the same index structure as the ImageFit enum
      this.sendNotification(`Cycling Scale: ${text}`)
    },
    toggleDoublePages () {
      this.doublePages = !this.doublePages
      this.sendNotification(`${this.doublePages ? 'Enabled' : 'Disabled'} Double Pages`)
    },
    sendNotification (message: string, timeout: number = 4000) {
      this.notification.timeout = 4000
      this.notification.message = message
      this.notification.enabled = true
    },
    async markProgress (page: number) {
      await this.$komgaBooks.updateReadProgress(this.bookId, { page: page })
    },
  },
})
</script>

<style scoped>
.settings {
  /*position: absolute;*/
  z-index: 2;
}

.top {
  top: 0;
}

.full-height {
  height: 100%;
}

.full-width {
  width: 100%;
}

.left-quarter {
  left: 0;
  width: 20%;
  position: absolute;
}

.right-quarter {
  right: 0;
  width: 20%;
  position: absolute;
}

.center-half {
  left: 20%;
  width: 60%;
  position: absolute;
}
</style>
