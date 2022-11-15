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

          <v-tooltip bottom v-if="incognito">
            <template v-slot:activator="{ on }">
              <v-icon v-on="on">mdi-incognito</v-icon>
            </template>
            <span>{{ $t('bookreader.tooltip_incognito') }}</span>
          </v-tooltip>

          <v-btn
            icon
            :disabled="!screenfull.isEnabled"
            @click="screenfull.isFullscreen ? screenfull.exit() : enterFullscreen()">
            <v-icon>{{ fullscreenIcon }}</v-icon>
          </v-btn>

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

          <v-menu offset-y>
            <template v-slot:activator="{ on }">
              <v-btn icon v-on="on" @click.prevent="">
                <v-icon>mdi-dots-vertical</v-icon>
              </v-btn>
            </template>
            <v-list>
              <v-list-item @click="downloadCurrentPage">
                <v-list-item-title>{{ $t('bookreader.download_current_page') }}</v-list-item-title>
              </v-list-item>
              <v-list-item @click="setCurrentPageAsPoster(ItemTypes.BOOK)">
                <v-list-item-title>{{ $t('bookreader.set_current_page_as_book_poster') }}</v-list-item-title>
              </v-list-item>
              <v-list-item @click="setCurrentPageAsPoster(ItemTypes.SERIES)">
                <v-list-item-title>{{ $t('bookreader.set_current_page_as_series_poster') }}</v-list-item-title>
              </v-list-item>
              <v-list-item v-if="contextReadList"  @click="setCurrentPageAsPoster(ItemTypes.READLIST)">
                <v-list-item-title>{{ $t('bookreader.set_current_page_as_readlist_poster') }}</v-list-item-title>
              </v-list-item>
            </v-list>
          </v-menu>
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
      <PSPDFKitReader
        v-if="PDFReader"
        :pages="pages"
        :page.sync="page"
        :bookId="bookId"
        @menu="toggleToolbars()"
        @jump-previous="jumpToPrevious()"
        @jump-next="jumpToNext()"
      ></PSPDFKitReader>

      <continuous-reader
        v-else-if="continuousReader"
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
        :page-layout="pageLayout"
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
          <v-toolbar-title>{{ $t('bookreader.reader_settings') }}</v-toolbar-title>
        </v-toolbar>

        <v-card-text class="pa-0">
          <v-list class="full-height full-width">
            <v-subheader class="font-weight-black text-h6">{{ $t('bookreader.settings.general') }}</v-subheader>
            <v-list-item>
              <settings-select
                :items="readingDirs"
                v-model="readingDirection"
                :label="$t('bookreader.settings.reading_mode')"
              />
            </v-list-item>

            <v-list-item>
              <settings-switch v-model="animations"
                               :label="$t('bookreader.settings.animate_page_transitions')"/>
            </v-list-item>

            <v-list-item>
              <settings-switch v-model="swipe" :label="$t('bookreader.settings.gestures')"/>
            </v-list-item>

            <v-list-item>
              <settings-switch v-model="alwaysFullscreen" :label="$t('bookreader.settings.always_fullscreen')"
                               :disabled="!screenfull.isEnabled"/>
            </v-list-item>

            <v-subheader class="font-weight-black text-h6">{{ $t('bookreader.settings.display') }}</v-subheader>
            <v-list-item>
              <settings-select
                :items="backgroundColors"
                v-model="backgroundColor"
                :label="$t('bookreader.settings.background_color')"
              />
            </v-list-item>

            <template v-if="continuousReader">
              <v-subheader class="font-weight-black text-h6">{{ $t('bookreader.settings.webtoon') }}</v-subheader>
              <v-list-item>
                <settings-select
                  :items="continuousScaleTypes"
                  v-model="continuousScale"
                  :label="$t('bookreader.settings.scale_type')"
                />
              </v-list-item>
              <v-list-item>
                <settings-select
                  :items="paddingPercentages"
                  v-model="sidePadding"
                  :label="$t('bookreader.settings.side_padding')"
                />
              </v-list-item>
            </template>

            <template v-if="!continuousReader">
              <v-subheader class="font-weight-black text-h6">{{ $t('bookreader.settings.paged') }}</v-subheader>
              <v-list-item>
                <settings-select
                  :items="scaleTypes"
                  v-model="scale"
                  :label="$t('bookreader.settings.scale_type')"
                />
              </v-list-item>

              <v-list-item>
                <settings-select
                  :items="pageLayouts"
                  v-model="pageLayout"
                  :label="$t('bookreader.settings.page_layout')"
                />
              </v-list-item>
            </template>

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
        <p>{{ $t('bookreader.beginning_of_book') }}</p>
        <p v-if="!$_.isEmpty(siblingPrevious)">{{ $t('bookreader.move_previous') }}</p>
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
        <p>{{ $t('bookreader.end_of_book') }}</p>
        <p v-if="!$_.isEmpty(siblingNext)">{{ $t('bookreader.move_next') }}</p>
        <p v-else>{{ $t('bookreader.move_next_exit') }}</p>
      </div>
    </v-snackbar>

    <v-snackbar
      v-model="notificationReadingDirection.enabled"
      color="rgba(0, 0, 0, 0.8)"
      bottom
      timeout="3000"
    >
      <p class="text-body-1 text-center ma-0">
        {{
          readingDirectionText
        }}{{ notificationReadingDirection.fromMetadata ? '(' + $t('bookreader.from_series_metadata') + ')' : '' }}
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
import {debounce} from 'lodash'
import SettingsSelect from '@/components/SettingsSelect.vue'
import SettingsSwitch from '@/components/SettingsSwitch.vue'
import ThumbnailExplorerDialog from '@/components/dialogs/ThumbnailExplorerDialog.vue'
import ShortcutHelpDialog from '@/components/dialogs/ShortcutHelpDialog.vue'
import {getBookTitleCompact} from '@/functions/book-title'
import {checkImageSupport, ImageFeature} from '@/functions/check-image'
import {bookPageUrl} from '@/functions/urls'
import {getFileFromUrl} from '@/functions/file'
import {resizeImageFile} from '@/functions/resize-image'
import {ReadingDirection} from '@/types/enum-books'
import Vue from 'vue'
import {Location} from 'vue-router'
import PagedReader from '@/components/readers/PagedReader.vue'
import ContinuousReader from '@/components/readers/ContinuousReader.vue'
import PSPDFKitReader from '@/components/readers/PSPDFKitReader.vue'
import {ContinuousScaleType, PaddingPercentage, PagedReaderLayout, ScaleType} from '@/types/enum-reader'
import {
  shortcutsLTR,
  shortcutsRTL,
  shortcutsSettingsPaged,
  shortcutsVertical,
} from '@/functions/shortcuts/paged-reader'
import {shortcutsMenus, shortcutsSettings} from '@/functions/shortcuts/bookreader'
import {shortcutsAll} from '@/functions/shortcuts/reader'
import {shortcutsSettingsContinuous} from '@/functions/shortcuts/continuous-reader'
import {BookDto, PageDto, PageDtoWithUrl} from '@/types/komga-books'
import {Context, ContextOrigin} from '@/types/context'
import {SeriesDto} from '@/types/komga-series'
import jsFileDownloader from 'js-file-downloader'
import screenfull from 'screenfull'
import {ItemTypes} from '@/types/items'

export default Vue.extend({
  name: 'BookReader',
  components: {
    ContinuousReader,
    PSPDFKitReader,
    PagedReader,
    SettingsSwitch,
    SettingsSelect,
    ThumbnailExplorerDialog,
    ShortcutHelpDialog,
  },
  data: function () {
    return {
      ItemTypes,
      screenfull,
      fullscreenIcon: 'mdi-fullscreen',
      book: {} as BookDto,
      series: {} as SeriesDto,
      context: {} as Context,
      contextName: '',
      incognito: false,
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
      page: undefined as unknown as number,
      supportedMediaTypes: ['image/jpeg', 'image/png', 'image/gif'],
      convertTo: 'jpeg',
      showExplorer: false,
      showToolbars: false,
      showSettings: false,
      showHelp: false,
      goToPage: 1,
      settings: {
        pageLayout: PagedReaderLayout.SINGLE_PAGE,
        swipe: false,
        alwaysFullscreen: false,
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
        text: this.$i18n.t(`enums.reading_direction.${x}`),
        value: x,
      })),
      scaleTypes: Object.values(ScaleType).map(x => ({
        text: this.$i18n.t(x),
        value: x,
      })),
      continuousScaleTypes: Object.values(ContinuousScaleType).map(x => ({
        text: this.$i18n.t(x),
        value: x,
      })),
      pageLayouts: Object.values(PagedReaderLayout).map(x => ({
        text: this.$i18n.t(x),
        value: x,
      })),
      paddingPercentages: Object.values(PaddingPercentage).map(x => ({
        text: x === 0 ? this.$i18n.t('bookreader.settings.side_padding_none').toString() : `${x}%`,
        value: x,
      })),
      backgroundColors: [
        {text: this.$t('bookreader.settings.background_colors.white').toString(), value: 'white'},
        {text: this.$t('bookreader.settings.background_colors.gray').toString(), value: '#212121'},
        {text: this.$t('bookreader.settings.background_colors.black').toString(), value: 'black'},
      ],
    }
  },
  created() {
    this.$vuetify.rtl = false
    checkImageSupport(ImageFeature.WEBP_LOSSY, (isSupported) => {
      if (isSupported) this.supportedMediaTypes.push('image/webp')
    })
    checkImageSupport(ImageFeature.JPEG_XL, (isSupported) => {
      if (isSupported) this.supportedMediaTypes.push('image/jxl')
    })
    this.shortcuts = this.$_.keyBy([...shortcutsSettings, ...shortcutsSettingsPaged, ...shortcutsSettingsContinuous, ...shortcutsMenus, ...shortcutsAll], x => x.key)
    window.addEventListener('keydown', this.keyPressed)
    if (screenfull.isEnabled) screenfull.on('change', this.fullscreenChanged)
  },
  async mounted() {
    document.documentElement.classList.add('html-reader')

    this.$debug('[mounted]', 'route.query:', this.$route.query)

    this.readingDirection = this.$store.state.persistedState.webreader.readingDirection
    this.animations = this.$store.state.persistedState.webreader.animations
    this.pageLayout = this.$store.state.persistedState.webreader.paged.pageLayout
    this.swipe = this.$store.state.persistedState.webreader.swipe
    this.alwaysFullscreen = this.$store.state.persistedState.webreader.alwaysFullscreen
    this.scale = this.$store.state.persistedState.webreader.paged.scale
    this.continuousScale = this.$store.state.persistedState.webreader.continuous.scale
    this.sidePadding = this.$store.state.persistedState.webreader.continuous.padding
    this.backgroundColor = this.$store.state.persistedState.webreader.background

    this.setup(this.bookId, Number(this.$route.query.page))
  },
  destroyed() {
    document.documentElement.classList.remove('html-reader')

    this.$vuetify.rtl = (this.$t('common.locale_rtl') === 'true')
    window.removeEventListener('keydown', this.keyPressed)
    if (screenfull.isEnabled) {
      screenfull.off('change', this.fullscreenChanged)
      screenfull.exit()
    }
  },
  props: {
    bookId: {
      type: String,
      required: true,
    },
  },
  async beforeRouteUpdate(to, from, next) {
    if (to.params.bookId !== from.params.bookId) {
      // route update means either:
      // - going to previous/next book, in this case the query.page is not set, so it will default to first page
      // - pressing the back button of the browser and navigating to the previous book, in this case the query.page is set, so we honor it
      this.$debug('[beforeRouteUpdate]', 'to.query:', to.query)
      this.setup(to.params.bookId, Number(to.query.page))
    }
    next()
  },
  watch: {
    page: {
      handler(val, old) {
        if (val) {
          this.markProgress(val)
          this.goToPage = val
          this.updateRoute()
        }
      },
      immediate: true,
    },
  },
  computed: {
    continuousReader(): boolean {
      return this.readingDirection === ReadingDirection.WEBTOON
    },
    PDFReader():boolean{
      return this.readingDirection===ReadingDirection.PDF
    },
    progress(): number {
      return this.page / this.pagesCount * 100
    },
    pagesCount(): number {
      return this.pages.length
    },
    bookTitle(): string {
      return getBookTitleCompact(this.book.metadata.title, this.series.metadata.title)
    },
    readingDirectionText(): string {
      return this.$t(`enums.reading_direction.${this.readingDirection}`).toString()
    },
    shortcutsHelp(): object {
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
        [this.$t('bookreader.shortcuts.reader_navigation').toString()]: nav,
        [this.$t('bookreader.shortcuts.settings').toString()]: settings,
        [this.$t('bookreader.shortcuts.menus').toString()]: shortcutsMenus,
      }
    },
    contextReadList(): boolean {
      return this.context.origin === ContextOrigin.READLIST
    },
    currentPage(): PageDtoWithUrl {
      return this.pages[this.page - 1]
    },

    animations: {
      get: function (): boolean {
        return this.settings.animations
      },
      set: function (animations: boolean): void {
        this.settings.animations = animations
        this.$store.commit('setWebreaderAnimations', animations)
      },
    },
    scale: {
      get: function (): ScaleType {
        return this.settings.scale
      },
      set: function (scale: ScaleType): void {
        if (Object.values(ScaleType).includes(scale)) {
          this.settings.scale = scale
          this.$store.commit('setWebreaderPagedScale', scale)
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
          this.$store.commit('setWebreaderContinuousScale', scale)
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
          this.$store.commit('setWebreaderContinuousPadding', padding)
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
          this.$store.commit('setWebreaderBackground', color)
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
          this.$store.commit('setWebreaderReadingDirection', readingDirection)
        }
      },
    },
    pageLayout: {
      get: function (): PagedReaderLayout {
        return this.settings.pageLayout
      },
      set: function (pageLayout: PagedReaderLayout): void {
        if (Object.values(PagedReaderLayout).includes(pageLayout)) {
          this.settings.pageLayout = pageLayout
          this.$store.commit('setWebreaderPagedPageLayout', pageLayout)
        }
      },
    },
    swipe: {
      get: function (): boolean {
        return this.settings.swipe
      },
      set: function (swipe: boolean): void {
        this.settings.swipe = swipe
        this.$store.commit('setWebreaderSwipe', swipe)
      },
    },
    alwaysFullscreen: {
      get: function (): boolean {
        return this.settings.alwaysFullscreen
      },
      set: function (alwaysFullscreen: boolean): void {
        this.settings.alwaysFullscreen = alwaysFullscreen
        this.$store.commit('setWebreaderAlwaysFullscreen', alwaysFullscreen)
        if (alwaysFullscreen) this.enterFullscreen()
        else screenfull.isEnabled && screenfull.exit()
      },
    },
  },
  methods: {
    enterFullscreen() {
      if (screenfull.isEnabled) screenfull.request(document.documentElement, {navigationUI: 'hide'})
    },
    switchFullscreen() {
      if (screenfull.isEnabled) screenfull.isFullscreen ? screenfull.exit() : this.enterFullscreen()
    },
    fullscreenChanged() {
      if (screenfull.isEnabled && screenfull.isFullscreen) this.fullscreenIcon = 'mdi-fullscreen-exit'
      else this.fullscreenIcon = 'mdi-fullscreen'
    },
    keyPressed(e: KeyboardEvent) {
      if (e.ctrlKey || e.altKey || e.shiftKey || e.metaKey) return
      this.shortcuts[e.key]?.execute(this)
    },
    async setup(bookId: string, page?: number) {
      this.$debug('[setup]', `bookId:${bookId}`, `page:${page}`)
      this.book = await this.$komgaBooks.getBook(bookId)
      this.series = await this.$komgaSeries.getOneSeries(this.book.seriesId)

      // parse query params to get context and contextId
      if (this.$route.query.contextId && this.$route.query.context
        && Object.values(ContextOrigin).includes(this.$route.query.context as ContextOrigin)) {
        this.context = {
          origin: this.$route.query.context as ContextOrigin,
          id: this.$route.query.contextId as string,
        }
        this.book.context = this.context
        this.contextName = (await (this.$komgaReadLists.getOneReadList(this.context.id))).name
        document.title = `Komga - ${this.contextName} - ${this.book.metadata.title}`
      } else {
        document.title = `Komga - ${getBookTitleCompact(this.book.metadata.title, this.series.metadata.title)}`
      }

      // parse query params to get incognito mode
      this.incognito = !!(this.$route.query.incognito && this.$route.query.incognito.toString().toLowerCase() === 'true')

      const pageDtos = (await this.$komgaBooks.getBookPages(bookId))
      pageDtos.forEach((p: any) => p['url'] = this.getPageUrl(p))
      this.pages = pageDtos as PageDtoWithUrl[]

      this.$debug('[setup]', `pages count:${this.pagesCount}`, 'read progress:', this.book.readProgress)
      if (page && page >= 1 && page <= this.pagesCount) {
        this.goTo(page)
      } else if (this.book.readProgress?.completed === false) {
        this.goTo(this.book.readProgress?.page!!)
      } else {
        this.goToFirst()
      }

      // set non-persistent reading direction if exists in metadata
      if (this.series.metadata.readingDirection in ReadingDirection && this.readingDirection !== this.series.metadata.readingDirection) {
        // bypass setter so setting is not persisted
        this.settings.readingDirection = this.series.metadata.readingDirection as ReadingDirection
        this.sendNotificationReadingDirection(true)
      } else {
        this.sendNotificationReadingDirection(false)
      }

      try {
        if (this?.context.origin === ContextOrigin.READLIST) {
          this.siblingNext = await this.$komgaReadLists.getBookSiblingNext(this.context.id, bookId)
        } else {
          this.siblingNext = await this.$komgaBooks.getBookSiblingNext(bookId)
        }
      } catch (e) {
        this.siblingNext = {} as BookDto
      }
      try {
        if (this?.context.origin === ContextOrigin.READLIST) {
          this.siblingPrevious = await this.$komgaReadLists.getBookSiblingPrevious(this.context.id, bookId)
        } else {
          this.siblingPrevious = await this.$komgaBooks.getBookSiblingPrevious(bookId)
        }
      } catch (e) {
        this.siblingPrevious = {} as BookDto
      }
    },
    getPageUrl(page: PageDto): string {
      if (!this.supportedMediaTypes.includes(page.mediaType)) {
        return bookPageUrl(this.bookId, page.number, this.convertTo)
      } else {
        return bookPageUrl(this.bookId, page.number)
      }
    },
    jumpToPrevious() {
      if (this.jumpToPreviousBook) {
        this.previousBook()
      } else {
        this.jumpToPreviousBook = true
      }
    },
    jumpToNext() {
      if (this.jumpToNextBook) {
        this.nextBook()
      } else {
        this.jumpToNextBook = true
      }
    },
    previousBook() {
      if (!this.$_.isEmpty(this.siblingPrevious)) {
        this.jumpToPreviousBook = false
        this.$router.push({
          name: 'read-book',
          params: {bookId: this.siblingPrevious.id.toString()},
          query: {context: this.context.origin, contextId: this.context.id, incognito: this.incognito.toString()},
        })
      }
    },
    nextBook() {
      if (this.$_.isEmpty(this.siblingNext)) {
        this.closeBook()
      } else {
        this.jumpToNextBook = false
        this.$router.push({
          name: 'read-book',
          params: {bookId: this.siblingNext.id.toString()},
          query: {context: this.context.origin, contextId: this.context.id, incognito: this.incognito.toString()},
        })
      }
    },
    goTo(page: number) {
      this.$debug('[goTo]', `page:${page}`)
      this.page = page
      this.markProgress(page)
    },
    goToFirst() {
      this.goTo(1)
    },
    goToLast() {
      this.goTo(this.pagesCount)
    },
    updateRoute() {
      this.$router.replace({
        name: this.$route.name,
        params: {bookId: this.$route.params.bookId},
        query: {
          page: this.page.toString(),
          context: this.context.origin,
          contextId: this.context.id,
          incognito: this.incognito.toString(),
        },
      } as Location)
    },
    closeBook() {
      this.$router.push({
        name: 'browse-book',
        params: {bookId: this.bookId.toString()},
        query: {context: this.context.origin, contextId: this.context.id},
      })
    },
    changeReadingDir(dir: ReadingDirection) {
      this.readingDirection = dir
      const text = this.$t(`enums.reading_direction.${this.readingDirection}`)
      this.sendNotification(`${this.$t('bookreader.changing_reading_direction')}: ${text}`)
    },
    cycleScale() {
      if (this.continuousReader) {
        const enumValues = Object.values(ContinuousScaleType)
        const i = (enumValues.indexOf(this.settings.continuousScale) + 1) % (enumValues.length)
        this.continuousScale = enumValues[i]
        const text = this.$t(this.continuousScale)
        this.sendNotification(`${this.$t('bookreader.cycling_scale')}: ${text}`)
      } else {
        const enumValues = Object.values(ScaleType)
        const i = (enumValues.indexOf(this.settings.scale) + 1) % (enumValues.length)
        this.scale = enumValues[i]
        const text = this.$t(this.scale)
        this.sendNotification(`${this.$t('bookreader.cycling_scale')}: ${text}`)
      }
    },
    cycleSidePadding() {
      if (this.continuousReader) {
        const i = (PaddingPercentage.indexOf(this.settings.sidePadding) + 1) % (PaddingPercentage.length)
        this.sidePadding = PaddingPercentage[i]
        const text = this.sidePadding === 0 ? this.$t('bookreader.settings.side_padding_none').toString() : `${this.sidePadding}%`
        this.sendNotification(`${this.$t('bookreader.cycling_side_padding')}: ${text}`)
      }
    },
    cyclePageLayout() {
      if (this.continuousReader) return
      const enumValues = Object.values(PagedReaderLayout)
      const i = (enumValues.indexOf(this.settings.pageLayout) + 1) % (enumValues.length)
      this.pageLayout = enumValues[i]
      const text = this.$i18n.t(this.pageLayout)
      this.sendNotification(`${this.$t('bookreader.cycling_page_layout')}: ${text}`)
    },
    toggleToolbars() {
      this.showToolbars = !this.showToolbars
    },
    toggleExplorer() {
      this.showExplorer = !this.showExplorer
    },
    toggleSettings() {
      this.showSettings = !this.showSettings
    },
    toggleHelp() {
      this.showHelp = !this.showHelp
    },
    closeDialog() {
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
    sendNotificationReadingDirection(fromMetadata: boolean) {
      this.notificationReadingDirection.fromMetadata = fromMetadata
      this.notificationReadingDirection.enabled = true
    },
    sendNotification(message: string, timeout: number = 4000) {
      this.notification.timeout = timeout
      this.notification.message = message
      this.notification.enabled = true
    },
    markProgress: debounce(function (this: any, page: number) {
      if (!this.incognito) {
        this.$komgaBooks.updateReadProgress(this.bookId, {page: page})
      }
    }, 50),
    downloadCurrentPage() {
      new jsFileDownloader({
        url: this.currentPage.url,
        withCredentials: true,
        forceDesktopMode: true,
      })
    },
    async setCurrentPageAsPoster(type: ItemTypes) {
      const imageFile = await getFileFromUrl(this.currentPage.url, 'poster', 'image/jpeg', {credentials: 'include'})
      const newImageFile = await resizeImageFile(imageFile)
      switch (type) {
        case ItemTypes.BOOK:
          await this.$komgaBooks.uploadThumbnail(this.book.id, newImageFile, true)
          this.sendNotification(`${this.$t('bookreader.notification_poster_set_book')}`)
          break
        case ItemTypes.SERIES:
          await this.$komgaSeries.uploadThumbnail(this.series.id, newImageFile, true)
          this.sendNotification(`${this.$t('bookreader.notification_poster_set_series')}`)
          break
        case ItemTypes.READLIST:
          await this.$komgaReadLists.uploadThumbnail(this.context.id, newImageFile, true)
          this.sendNotification(`${this.$t('bookreader.notification_poster_set_readlist')}`)
          break
      }
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
<style>
.html-reader::-webkit-scrollbar {
  display: none;
}

.html-reader {
  scrollbar-width: none;
}
</style>
