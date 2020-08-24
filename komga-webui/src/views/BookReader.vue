<template>
  <v-container class="ma-0 pa-0 full-height" fluid v-if="pages.length > 0"
               :style="`width: 100%; background-color: ${backgroundColor}`"
  >
    <div>
      <v-slide-y-transition>
        <!-- Top Toolbar-->
        <v-toolbar
          dense elevation="1"
          v-if="showToolbars"
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

          <v-btn
            icon
            @click="showHelp = !showHelp">
            <v-icon>mdi-help-circle</v-icon>
          </v-btn>

          <v-btn
            icon
            @click="showExplorer = !showExplorer"
          >
            <v-icon>mdi-view-grid</v-icon>
          </v-btn>
          <v-btn
            icon
            @click="showSettings = !showSettings"
          >
            <v-icon>mdi-cog</v-icon>
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
          v-if="showToolbars"
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
                    {{ page }}
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

    <div class="full-height">
      <continuous-reader
        v-if="continuousReader"
        :pages="pages"
        :page.sync="page"
        :animations="animations"
        :scale="continuousScale"
        :sidePadding="sidePadding"
        @menu="toggleToolbars()"
        @jump-previous="jumpToPrevious()"
        @jump-next="jumpToNext()"
      ></continuous-reader>

      <paged-reader
        v-else
        :pages="pages"
        :page.sync="page"
        :reading-direction="readingDirection"
        :double-pages="doublePages"
        :scale="scale"
        :animations="animations"
        :swipe="swipe"
        @menu="toggleToolbars()"
        @jump-previous="jumpToPrevious()"
        @jump-next="jumpToNext()"
      ></paged-reader>
    </div>

    <thumbnail-explorer-dialog
      v-model="showExplorer"
      :bookId="bookId"
      @go="goTo"
      :pagesCount="pagesCount"
    ></thumbnail-explorer-dialog>

    <v-bottom-sheet
      v-model="showSettings"
      :close-on-content-click="false"
      max-width="500"
      @keydown.esc.stop=""
      scrollable
    >
      <v-card>
        <v-toolbar dark color="primary">
          <v-btn icon dark @click="showSettings = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
          <v-toolbar-title>Reader Settings</v-toolbar-title>
        </v-toolbar>

        <v-card-text class="pa-0">
          <v-list class="full-height full-width">
            <v-subheader class="font-weight-black text-h6">General</v-subheader>
            <v-list-item>
              <settings-select
                :items="readingDirs"
                v-model="readingDirection"
                label="Reading mode"
              />
            </v-list-item>

            <v-list-item>
              <settings-switch v-model="animations" label="Animate page transitions"></settings-switch>
            </v-list-item>

            <v-list-item>
              <settings-switch v-model="swipe" label="Gestures"></settings-switch>
            </v-list-item>

            <v-subheader class="font-weight-black text-h6">Display</v-subheader>
            <v-list-item>
              <settings-select
                :items="backgroundColors"
                v-model="backgroundColor"
                label="Background color"
              >
              </settings-select>
            </v-list-item>

            <div v-if="continuousReader">
              <v-subheader class="font-weight-black text-h6">Webtoon</v-subheader>
              <v-list-item>
                <settings-select
                  :items="continuousScaleTypes"
                  v-model="continuousScale"
                  label="Scale type"
                />
              </v-list-item>
              <v-list-item>
                <settings-select
                  :items="paddingPercentages"
                  v-model="sidePadding"
                  label="Side padding"
                />
              </v-list-item>
            </div>

            <div v-if="!continuousReader">
              <v-subheader class="font-weight-black text-h6">Paged</v-subheader>
              <v-list-item>
                <settings-select
                  :items="scaleTypes"
                  v-model="scale"
                  label="Scale type"
                />
              </v-list-item>

              <v-list-item>
                <settings-switch v-model="doublePages" label="Double pages"></settings-switch>
              </v-list-item>
            </div>


          </v-list>
        </v-card-text>
      </v-card>
    </v-bottom-sheet>
    <v-snackbar
      v-model="jumpToPreviousBook"
      :timeout="jumpConfirmationDelay"
      top
      color="rgba(0, 0, 0, 0.8)"
      multi-line
      class="mt-12"
    >
      <div class="body-1 pa-6">
        <p>You're at the beginning<br/>of the book.</p>
        <p v-if="!$_.isEmpty(siblingPrevious)">Click or press previous again<br/>to move to the previous book.</p>
      </div>
    </v-snackbar>

    <v-snackbar
      v-model="jumpToNextBook"
      :timeout="jumpConfirmationDelay"
      top
      color="rgba(0, 0, 0, 0.8)"
      multi-line
      class="mt-12"
    >
      <div class="text-body-1 pa-6">
        <p>You've reached the end<br/>of the book.</p>
        <p v-if="!$_.isEmpty(siblingNext)">Click or press next again<br/>to move to the next book.</p>
        <p v-else>Click or press next again<br/>to exit the reader.</p>
      </div>
    </v-snackbar>

    <v-snackbar
      v-model="notificationReadingDirection.enabled"
      color="rgba(0, 0, 0, 0.8)"
      bottom
      timeout="3000"
    >
      <p class="text-body-1 text-center ma-0">
        {{ readingDirectionText }}{{ notificationReadingDirection.fromMetadata ? ' (from book metadata)' : '' }}
      </p>
    </v-snackbar>

    <v-snackbar
      v-model="notification.enabled"
      color="rgba(0, 0, 0, 0.8)"
      centered
      :timeout="notification.timeout"
    >
      <p class="text-h6 text-center ma-0">
        {{ notification.message }}
      </p>
    </v-snackbar>

    <shortcut-help-dialog
      v-model="showHelp"
      :shortcuts="shortcutsHelp"
    />
  </v-container>
</template>

<script lang="ts">
import SettingsSelect from '@/components/SettingsSelect.vue'
import SettingsSwitch from '@/components/SettingsSwitch.vue'
import ThumbnailExplorerDialog from '@/components/dialogs/ThumbnailExplorerDialog.vue'
import ShortcutHelpDialog from '@/components/dialogs/ShortcutHelpDialog.vue'
import { getBookTitleCompact } from '@/functions/book-title'
import { checkWebpFeature } from '@/functions/check-webp'
import { bookPageUrl } from '@/functions/urls'
import { ReadingDirection } from '@/types/enum-books'
import Vue from 'vue'
import { Location } from 'vue-router'
import PagedReader from '@/components/readers/PagedReader.vue'
import ContinuousReader from '@/components/readers/ContinuousReader.vue'
import { ContinuousScaleType, PaddingPercentage, ScaleType } from '@/types/enum-reader'
import { ReadingDirectionText, ScaleTypeText } from '@/functions/reader'
import {
  shortcutsLTR,
  shortcutsRTL,
  shortcutsSettingsPaged,
  shortcutsVertical,
} from '@/functions/shortcuts/paged-reader'
import { shortcutsMenus, shortcutsSettings } from '@/functions/shortcuts/bookreader'
import { shortcutsAll } from '@/functions/shortcuts/reader'
import { shortcutsSettingsContinuous } from '@/functions/shortcuts/continuous-reader'

const cookieFit = 'webreader.fit'
const cookieContinuousReaderFit = 'webreader.continuousReaderFit'
const cookieContinuousReaderPadding = 'webreader.continuousReaderPadding'
const cookieReadingDirection = 'webreader.readingDirection'
const cookieDoublePages = 'webreader.doublePages'
const cookieSwipe = 'webreader.swipe'
const cookieAnimations = 'webreader.animations'
const cookieBackground = 'webreader.background'

export default Vue.extend({
  name: 'BookReader',
  components: {
    ContinuousReader,
    PagedReader,
    SettingsSwitch,
    SettingsSelect,
    ThumbnailExplorerDialog,
    ShortcutHelpDialog,
  },
  data: () => {
    return {
      book: {} as BookDto,
      series: {} as SeriesDto,
      siblingPrevious: {} as BookDto,
      siblingNext: {} as BookDto,
      jumpToNextBook: false,
      jumpToPreviousBook: false,
      jumpConfirmationDelay: 3000,
      notificationReadingDirection: {
        enabled: false,
        fromMetadata: false,
      },
      pages: [] as PageDtoWithUrl[],
      page: 1,
      supportedMediaTypes: ['image/jpeg', 'image/png', 'image/gif'],
      convertTo: 'jpeg',
      showExplorer: false,
      showToolbars: false,
      showSettings: false,
      showHelp: false,
      goToPage: 1,
      settings: {
        doublePages: false,
        swipe: true,
        animations: true,
        scale: ScaleType.SCREEN,
        continuousScale: ContinuousScaleType.WIDTH,
        sidePadding: 0,
        readingDirection: ReadingDirection.LEFT_TO_RIGHT,
        backgroundColor: 'black',
      },
      shortcuts: {} as any,
      notification: {
        enabled: false,
        message: '',
        timeout: 4000,
      },
      readingDirs: Object.values(ReadingDirection).map(x => ({
        text: ReadingDirectionText[x],
        value: x,
      })),
      scaleTypes: Object.values(ScaleType).map(x => ({
        text: ScaleTypeText[x],
        value: x,
      })),
      continuousScaleTypes: Object.values(ContinuousScaleType).map(x => ({
        text: ScaleTypeText[x],
        value: x,
      })),
      paddingPercentages: Object.values(PaddingPercentage).map(x => ({
        text: x === 0 ? 'None' : `${x}%`,
        value: x,
      })),
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
    this.shortcuts = this.$_.keyBy([...shortcutsSettings, ...shortcutsSettingsPaged, ...shortcutsSettingsContinuous, ...shortcutsMenus, ...shortcutsAll], x => x.key)
    window.addEventListener('keydown', this.keyPressed)
  },
  async mounted () {
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
      this.scale = v
    })
    this.loadFromCookie(cookieContinuousReaderFit, (v) => {
      this.continuousScale = v
    })
    this.loadFromCookie(cookieContinuousReaderPadding, (v) => {
      this.sidePadding = parseInt(v)
    })
    this.loadFromCookie(cookieBackground, (v) => {
      this.backgroundColor = v
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
    page (val) {
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
    continuousReader (): boolean {
      return this.readingDirection === ReadingDirection.WEBTOON
    },
    progress (): number {
      return this.page / this.pagesCount * 100
    },
    pagesCount (): number {
      return this.pages.length
    },
    bookTitle (): string {
      return getBookTitleCompact(this.book.metadata.title, this.series.metadata.title)
    },
    readingDirectionText (): string {
      return ReadingDirectionText[this.readingDirection]
    },
    shortcutsHelp (): object {
      let nav = []
      switch (this.readingDirection) {
        case ReadingDirection.LEFT_TO_RIGHT:
          nav.push(...shortcutsLTR, ...shortcutsAll)
          break
        case ReadingDirection.RIGHT_TO_LEFT:
          nav.push(...shortcutsRTL, ...shortcutsAll)
          break
        case ReadingDirection.VERTICAL:
          nav.push(...shortcutsVertical, ...shortcutsAll)
          break
        default:
          nav.push(...shortcutsAll)
      }
      let settings = [...shortcutsSettings]
      if (this.continuousReader) {
        settings.push(...shortcutsSettingsContinuous)
      } else {
        settings.push(...shortcutsSettingsPaged)
      }
      return {
        'Reader Navigation': nav,
        'Settings': settings,
        'Menus': shortcutsMenus,
      }
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
    scale: {
      get: function (): ScaleType {
        return this.settings.scale
      },
      set: function (scale: ScaleType): void {
        if (Object.values(ScaleType).includes(scale)) {
          this.settings.scale = scale
          this.$cookies.set(cookieFit, scale, Infinity)
        }
      },
    },
    continuousScale: {
      get: function (): ContinuousScaleType {
        return this.settings.continuousScale
      },
      set: function (scale: ContinuousScaleType): void {
        if (Object.values(ContinuousScaleType).includes(scale)) {
          this.settings.continuousScale = scale
          this.$cookies.set(cookieContinuousReaderFit, scale, Infinity)
        }
      },
    },
    sidePadding: {
      get: function (): number {
        return this.settings.sidePadding
      },
      set: function (padding: number): void {
        if (PaddingPercentage.includes(padding)) {
          this.settings.sidePadding = padding
          this.$cookies.set(cookieContinuousReaderPadding, padding, Infinity)
        }
      },
    },
    backgroundColor: {
      get: function (): string {
        return this.settings.backgroundColor
      },
      set: function (color: string): void {
        if (this.backgroundColors.map(x => x.value).includes(color)) {
          this.settings.backgroundColor = color
          this.$cookies.set(cookieBackground, color, Infinity)
        }
      },
    },
    readingDirection: {
      get: function (): ReadingDirection {
        return this.settings.readingDirection
      },
      set: function (readingDirection: ReadingDirection): void {
        if (Object.values(ReadingDirection).includes(readingDirection)) {
          this.settings.readingDirection = readingDirection
          this.$cookies.set(cookieReadingDirection, readingDirection, Infinity)
        }
      },
    },
    doublePages: {
      get: function (): boolean {
        return this.settings.doublePages
      },
      set: function (doublePages: boolean): void {
        this.settings.doublePages = doublePages
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
  },
  methods: {
    keyPressed (e: KeyboardEvent) {
      this.shortcuts[e.key]?.execute(this)
    },
    async setup (bookId: string, page: number) {
      this.book = await this.$komgaBooks.getBook(bookId)
      const pageDtos = (await this.$komgaBooks.getBookPages(bookId))
      pageDtos.forEach((p: any) => p['url'] = this.getPageUrl(p))
      this.pages = pageDtos as PageDtoWithUrl[]

      if (page >= 1 && page <= this.pagesCount) {
        this.goTo(page)
      } else if (this.book.readProgress?.completed === false) {
        this.goTo(this.book.readProgress?.page!!)
      } else {
        this.goToFirst()
      }

      // set non-persistent reading direction if exists in metadata
      if (this.book.metadata.readingDirection in ReadingDirection && this.readingDirection !== this.book.metadata.readingDirection) {
        // bypass setter so cookies aren't set
        this.settings.readingDirection = this.book.metadata.readingDirection as ReadingDirection
        this.sendNotificationReadingDirection(true)
      } else {
        this.sendNotificationReadingDirection(false)
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
    getPageUrl (page: PageDto): string {
      if (!this.supportedMediaTypes.includes(page.mediaType)) {
        return bookPageUrl(this.bookId, page.number, this.convertTo)
      } else {
        return bookPageUrl(this.bookId, page.number)
      }
    },
    jumpToPrevious () {
      if (this.jumpToPreviousBook) {
        this.previousBook()
      } else {
        this.jumpToPreviousBook = true
      }
    },
    jumpToNext () {
      if (this.jumpToNextBook) {
        this.nextBook()
      } else {
        this.jumpToNextBook = true
      }
    },
    previousBook () {
      if (!this.$_.isEmpty(this.siblingPrevious)) {
        this.jumpToPreviousBook = false
        this.$router.push({ name: 'read-book', params: { bookId: this.siblingPrevious.id.toString() } })
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
    goTo (page: number) {
      this.page = page
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
          page: this.page.toString(),
        },
      } as Location)
    },
    closeBook () {
      this.$router.push({ name: 'browse-book', params: { bookId: this.bookId.toString() } })
    },
    loadFromCookie (cookieKey: string, setter: (value: any) => void): void {
      if (this.$cookies.isKey(cookieKey)) {
        setter(this.$cookies.get(cookieKey))
      }
    },
    changeReadingDir (dir: ReadingDirection) {
      this.readingDirection = dir
      const text = ReadingDirectionText[this.readingDirection]
      this.sendNotification(`Changing Reading Direction to: ${text}`)
    },
    cycleScale () {
      if (this.continuousReader) {
        const enumValues = Object.values(ContinuousScaleType)
        const i = (enumValues.indexOf(this.settings.continuousScale) + 1) % (enumValues.length)
        this.continuousScale = enumValues[i]
        const text = ScaleTypeText[this.continuousScale]
        this.sendNotification(`Cycling Scale: ${text}`)
      } else {
        const enumValues = Object.values(ScaleType)
        const i = (enumValues.indexOf(this.settings.scale) + 1) % (enumValues.length)
        this.scale = enumValues[i]
        const text = ScaleTypeText[this.scale]
        this.sendNotification(`Cycling Scale: ${text}`)
      }
    },
    cycleSidePadding () {
      if (this.continuousReader) {
        const i = (PaddingPercentage.indexOf(this.settings.sidePadding) + 1) % (PaddingPercentage.length)
        this.sidePadding = PaddingPercentage[i]
        const text = this.sidePadding === 0 ? 'None' : `${this.sidePadding}%`
        this.sendNotification(`Cycling Side Padding: ${text}`)
      }
    },
    toggleDoublePages () {
      if (this.continuousReader) return
      this.doublePages = !this.doublePages
      this.sendNotification(`${this.doublePages ? 'Enabled' : 'Disabled'} Double Pages`)
    },
    toggleToolbars () {
      this.showToolbars = !this.showToolbars
    },
    toggleExplorer () {
      this.showExplorer = !this.showExplorer
    },
    toggleSettings () {
      this.showSettings = !this.showSettings
    },
    toggleHelp () {
      this.showHelp = !this.showHelp
    },
    closeDialog () {
      if (this.showExplorer) {
        this.showExplorer = false
        return
      }
      if (this.showSettings) {
        this.showSettings = false
        return
      }
      if (this.showToolbars) {
        this.showToolbars = false
        return
      }
      this.closeBook()
    },
    sendNotificationReadingDirection (fromMetadata: boolean) {
      this.notificationReadingDirection.fromMetadata = fromMetadata
      this.notificationReadingDirection.enabled = true
    },
    sendNotification (message: string, timeout: number = 4000) {
      this.notification.timeout = timeout
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
  z-index: 2;
}

.full-height {
  height: 100%;
}

.full-width {
  width: 100%;
}
</style>
