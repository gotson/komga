<template>
  <div id="root" :key="bookId">
    <v-slide-y-transition>
      <v-toolbar
        v-if="showToolbars"
        dense elevation="1"
        class="settings full-width"
        style="position: fixed; top: 0;z-index: 14"
      >
        <v-btn
          icon
          @click="closeBook"
        >
          <v-icon>mdi-arrow-left</v-icon>
        </v-btn>

        <v-btn
          :disabled="!hasToc && !hasLandmarks && !hasPageList"
          icon
          @click="showToc = !showToc">
          <v-icon>mdi-table-of-contents</v-icon>
        </v-btn>

        <v-toolbar-title> {{ bookTitle }}</v-toolbar-title>
        <v-spacer></v-spacer>

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

        <!--      <v-btn-->
        <!--        icon-->
        <!--        @click="showExplorer = !showExplorer"-->
        <!--      >-->
        <!--        <v-icon>mdi-view-grid</v-icon>-->
        <!--      </v-btn>-->

        <v-btn
          icon
          @click="toggleSettings"
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
        style="position: fixed; bottom: 0;z-index: 14"
        horizontal
        v-if="showToolbars"
      >
        <v-btn icon @click="previousBook">
          <v-icon>mdi-undo</v-icon>
        </v-btn>

        <v-spacer/>

        <v-btn
          icon
          :disabled="!historyCanGoBack"
          @click="historyBack"
        >
          <v-icon>mdi-chevron-left</v-icon>
        </v-btn>

        <v-btn
          icon
          :disabled="!historyCanGoForward"
          @click="historyForward"
        >
          <v-icon>mdi-chevron-right</v-icon>
        </v-btn>

        <v-spacer/>

        <v-btn icon @click="nextBook">
          <v-icon>mdi-redo</v-icon>
        </v-btn>
      </v-toolbar>
    </v-slide-y-reverse-transition>

    <v-navigation-drawer
      v-model="showToc"
      fixed
      temporary

      :width="$vuetify.breakpoint.smAndUp ? 500 : $vuetify.breakpoint.width - 50"
      style="z-index: 15"
    >
      <v-tabs grow>
        <v-tab v-if="hasToc">
          <v-icon>mdi-table-of-contents</v-icon>
        </v-tab>
        <v-tab v-if="hasLandmarks">
          <v-icon>mdi-eiffel-tower</v-icon>
        </v-tab>
        <v-tab v-if="hasPageList">
          <v-icon>mdi-numeric</v-icon>
        </v-tab>

        <v-tab-item v-if="hasToc" class="scrolltab">
          <toc-list :toc="tableOfContents" @goto="goToEntry" class="scrolltab-content"/>
        </v-tab-item>
        <v-tab-item v-if="hasLandmarks" class="scrolltab">
          <toc-list :toc="landmarks" @goto="goToEntry" class="scrolltab-content"/>
        </v-tab-item>
        <v-tab-item v-if="hasPageList" class="scrolltab">
          <toc-list :toc="pageList" @goto="goToEntry" class="scrolltab-content"/>
        </v-tab-item>
      </v-tabs>
    </v-navigation-drawer>

    <header id="headerMenu"/>

    <div id="D2Reader-Container" style="height: 100vh" :class="appearanceClass('bg')">
      <main tabindex=-1 id="iframe-wrapper" style="height: 100vh">
        <div id="reader-loading"></div>
        <div id="reader-error"></div>
        <div id="reader-info-top">
          <span class="book-title"></span>
        </div>
        <div id="reader-info-bottom">
          <div style="display: flex;justify-content: center;">
            <span id="chapter-position"></span>&nbsp;
            <span id="chapter-title"></span>
          </div>
        </div>
      </main>
      <a id="previous-chapter" rel="prev" role="button" aria-labelledby="previous-label"
         style="left: 50%;position: fixed;color: #000;height: 24px;background: #d3d3d33b; width: 150px;transform: translate(-50%, 0); display: block"
         :style="`top: ${showToolbars ? 48 : 0}px`"
      >
        <v-icon style="left: calc(50% - 12px); position: relative;">mdi-chevron-up</v-icon>
      </a>
      <a id="next-chapter" rel="next" role="button" aria-labelledby="next-label"
         style="bottom: 0;left: 50%;position: fixed;color: #000;height: 24px;background: #d3d3d33b; width: 150px;transform: translate(-50%, 0); display: block">
        <v-icon style="left: calc(50% - 12px);position: relative;">mdi-chevron-down</v-icon>
      </a>
    </div>

    <footer id="footerMenu">
      <a rel="prev" class="disabled" role="button" aria-labelledby="previous-label" style="top: 50%;left:0;position: fixed;height: 100px;
                    background: #d3d3d33b;">
        <v-icon style="top: calc(50% - 12px);
                        position: relative;">mdi-chevron-left
        </v-icon>
      </a>
      <a rel="next" class="disabled" role="button" aria-labelledby="next-label" style="top: 50%;right:0;position: fixed;height: 100px;
                    background: #d3d3d33b;">
        <v-icon style="top: calc(50% - 12px);position: relative;">mdi-chevron-right</v-icon>
      </a>
    </footer>

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
              <settings-switch v-model="alwaysFullscreen" :label="$t('bookreader.settings.always_fullscreen')"
                               :disabled="!screenfull.isEnabled"/>
            </v-list-item>

            <v-subheader class="font-weight-black text-h6">{{ $t('bookreader.settings.display') }}</v-subheader>

            <v-list-item>
              <v-list-item-title>{{ $t('epubreader.settings.viewing_theme') }}</v-list-item-title>
              <v-btn
                v-for="(a, i) in appearances"
                :key="i"
                :value="a.value"
                :color="a.color"
                :class="a.class"
                class="mx-1"
                @click="appearance = a.value"
              >
                <v-icon v-if="appearance === a.value">mdi-check</v-icon>
              </v-btn>
            </v-list-item>

            <v-list-item>
              <v-list-item-title>{{ $t('epubreader.settings.layout') }}</v-list-item-title>
              <v-btn-toggle mandatory v-model="verticalScroll" class="py-3">
                <v-btn :value="true">{{ $t('epubreader.settings.layout_scroll') }}</v-btn>
                <v-btn :value="false">{{ $t('epubreader.settings.layout_paginated') }}</v-btn>
              </v-btn-toggle>
            </v-list-item>

            <v-list-item v-if="!verticalScroll">
              <v-list-item-title>{{ $t('epubreader.settings.column_count') }}</v-list-item-title>
              <v-btn-toggle mandatory v-model="columnCount" class="py-3">
                <v-btn v-for="(c, i) in columnCounts" :key="i" :value="c.value">{{ c.text }}</v-btn>
              </v-btn-toggle>
            </v-list-item>

            <v-list-item class="justify-center">
              <v-btn depressed @click="fontSize-=10">
                <v-icon small>mdi-format-title</v-icon>
              </v-btn>
              <span class="caption mx-8" style="width: 2rem">{{ fontSize }}%</span>
              <v-btn depressed @click="fontSize+=10">
                <v-icon>mdi-format-title</v-icon>
              </v-btn>
            </v-list-item>

            <v-list-item class="justify-center">
              <v-btn depressed @click="lineHeight-=.1">
                <v-icon>$formatLineSpacingDown</v-icon>
              </v-btn>
              <span class="caption mx-8" style="width: 2rem">{{ Math.round(lineHeight * 100) }}%</span>
              <v-btn depressed @click="lineHeight+=.1">
                <v-icon>mdi-format-line-spacing</v-icon>
              </v-btn>
            </v-list-item>

            <v-list-item>
              <v-slider
                v-model="pageMargins"
                :label="$t('epubreader.settings.page_margins')"
                min="0.5"
                max="4"
                step="0.25"
                ticks="always"
                tick-size="3"
              />
            </v-list-item>
          </v-list>
        </v-card-text>
      </v-card>
    </v-bottom-sheet>

    <v-snackbar
      v-model="notification.enabled"
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
  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import D2Reader, {Locator} from '@d-i-t-a/reader'
import {bookManifestUrl} from '@/functions/urls'
import {BookDto} from '@/types/komga-books'
import {getBookTitleCompact} from '@/functions/book-title'
import {SeriesDto} from '@/types/komga-series'
import {Context, ContextOrigin} from '@/types/context'
import SettingsSwitch from '@/components/SettingsSwitch.vue'
import SettingsSelect from '@/components/SettingsSelect.vue'
import {TocEntry} from '@/types/epub'
import TocList from '@/components/TocList.vue'
import {Locations} from '@d-i-t-a/reader/dist/types/model/Locator'
import {epubShortcutsMenus, epubShortcutsSettings, shortcutsD2Reader} from '@/functions/shortcuts/epubreader'
import {flattenToc} from '@/functions/toc'
import ShortcutHelpDialog from '@/components/dialogs/ShortcutHelpDialog.vue'
import screenfull from 'screenfull'
import {getBookReadRouteFromMediaProfile} from '@/functions/book-format'

export default Vue.extend({
  name: 'EpubReader',
  components: {ShortcutHelpDialog, TocList, SettingsSelect, SettingsSwitch},
  data: function () {
    return {
      screenfull,
      fullscreenIcon: 'mdi-fullscreen',
      d2Reader: {} as D2Reader,
      book: undefined as unknown as BookDto,
      series: undefined as unknown as SeriesDto,
      siblingPrevious: {} as BookDto,
      siblingNext: {} as BookDto,
      incognito: false,
      context: {} as Context,
      contextName: '',
      showSettings: false,
      showToolbars: false,
      showToc: false,
      showHelp: false,
      shortcuts: {} as any,
      appearances: [
        {
          text: this.$t('enums.epubreader.appearances.day').toString(),
          value: 'readium-default-on',
          color: 'white',
          class: 'black--text',
        },
        {
          text: this.$t('enums.epubreader.appearances.sepia').toString(),
          value: 'readium-sepia-on',
          color: '#faf4e8',
          class: 'black--text',
        },
        {
          text: this.$t('enums.epubreader.appearances.night').toString(),
          value: 'readium-night-on',
          color: 'black',
          class: 'white--text',
        },
      ],
      columnCounts: [
        {text: this.$t('enums.epubreader.column_count.auto').toString(), value: 'auto'},
        {text: this.$t('enums.epubreader.column_count.one').toString(), value: '1'},
        {text: this.$t('enums.epubreader.column_count.two').toString(), value: '2'},
      ],
      settings: {
        appearance: 'readium-default-on',
        pageMargins: 1,
        lineHeight: 1,
        fontSize: 100,
        verticalScroll: false,
        columnCount: 'auto',
        alwaysFullscreen: false,
      },
      tocs: {
        toc: undefined as unknown as TocEntry[],
        landmarks: undefined as unknown as TocEntry[],
        pageList: undefined as unknown as TocEntry[],
      },
      currentLocation: undefined as unknown as Locator,
      historyCanGoBack: false,
      historyCanGoForward: false,
      notification: {
        enabled: false,
        message: '',
        timeout: 4000,
      },
      clickTimer: undefined,
      forceUpdate: false,
    }
  },
  created() {
    this.$vuetify.rtl = false
    this.shortcuts = this.$_.keyBy([...epubShortcutsSettings, ...epubShortcutsMenus], x => x.key)
    if (screenfull.isEnabled) screenfull.on('change', this.fullscreenChanged)
  },
  beforeDestroy() {
    this.d2Reader.stop()
  },
  destroyed() {
    this.$vuetify.rtl = (this.$t('common.locale_rtl') === 'true')
    if (screenfull.isEnabled) {
      screenfull.off('change', this.fullscreenChanged)
      screenfull.exit()
    }
  },
  mounted() {
    Object.assign(this.settings, this.$store.state.persistedState.epubreader)
    this.settings.alwaysFullscreen = this.$store.state.persistedState.webreader.alwaysFullscreen

    this.setup(this.bookId)
  },
  props: {
    bookId: {
      type: String,
      required: true,
    },
  },
  beforeRouteUpdate(to, from, next) {
    if (to.params.bookId !== from.params.bookId) {
      // route update means either:
      // - going to previous/next book, in this case the query.page is not set, so it will default to first page
      // - pressing the back button of the browser and navigating to the previous book, in this case the query.page is set, so we honor it
      this.d2Reader.stop()
      this.setup(to.params.bookId, Number(to.query.page))
    }
    next()
  },
  computed: {
    shortcutsHelp(): object {
      return {
        [this.$t('bookreader.shortcuts.reader_navigation').toString()]: [...shortcutsD2Reader],
        [this.$t('bookreader.shortcuts.settings').toString()]: [...epubShortcutsSettings],
        [this.$t('bookreader.shortcuts.menus').toString()]: epubShortcutsMenus,
      }
    },
    tableOfContents(): TocEntry[] {
      if (this.tocs.toc) return flattenToc(this.tocs.toc, 1, 0, this.currentLocation?.href)
      return []
    },
    landmarks(): TocEntry[] {
      if (this.tocs.landmarks) return flattenToc(this.tocs.landmarks, 1, 0, this.currentLocation?.href)
      return []
    },
    pageList(): TocEntry[] {
      if (this.tocs.pageList) return flattenToc(this.tocs.pageList, 1, 0, this.currentLocation?.href)
      return []
    },
    hasToc(): boolean {
      return this.tocs.toc?.length > 0
    },
    hasLandmarks(): boolean {
      return this.tocs.landmarks?.length > 0
    },
    hasPageList(): boolean {
      return this.tocs.pageList?.length > 0
    },
    bookTitle(): string {
      if (!!this.book && !!this.series)
        return getBookTitleCompact(this.book.metadata.title, this.series.metadata.title)
      return this.book?.metadata?.title
    },
    appearance: {
      get: function (): string {
        return this.settings.appearance
      },
      set: function (color: string): void {
        if (this.appearances.map(x => x.value).includes(color)) {
          this.settings.appearance = color
          this.d2Reader.applyUserSettings({appearance: color})
          this.$store.commit('setEpubreaderSettings', this.settings)
        }
      },
    },
    verticalScroll: {
      get: function (): boolean {
        return this.settings.verticalScroll
      },
      set: function (value: string): void {
        this.settings.verticalScroll = value
        this.d2Reader.applyUserSettings({verticalScroll: value})
        this.$store.commit('setEpubreaderSettings', this.settings)
      },
    },
    columnCount: {
      get: function (): boolean {
        return this.settings.columnCount
      },
      set: function (value: string): void {
        if (this.columnCounts.map(x => x.value).includes(value)) {
          this.settings.columnCount = value
          this.d2Reader.applyUserSettings({columnCount: value})
          this.$store.commit('setEpubreaderSettings', this.settings)
        }
      },
    },
    pageMargins: {
      get: function (): number {
        return this.settings.pageMargins
      },
      set: function (value: number): void {
        this.settings.pageMargins = value
        this.d2Reader.applyUserSettings({pageMargins: value})
        this.$store.commit('setEpubreaderSettings', this.settings)
      },
    },
    lineHeight: {
      get: function (): number {
        return this.settings.lineHeight
      },
      set: function (value: number): void {
        this.settings.lineHeight = value
        this.d2Reader.applyUserSettings({lineHeight: value})
        this.$store.commit('setEpubreaderSettings', this.settings)
      },
    },
    fontSize: {
      get: function (): number {
        return this.settings.fontSize
      },
      set: function (value: number): void {
        this.settings.fontSize = value
        this.d2Reader.applyUserSettings({fontSize: value})
        this.$store.commit('setEpubreaderSettings', this.settings)
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
    previousBook() {
      if (!this.$_.isEmpty(this.siblingPrevious)) {
        this.jumpToPreviousBook = false
        this.$router.push({
          name: getBookReadRouteFromMediaProfile(this.siblingPrevious.media.mediaProfile),
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
          name: getBookReadRouteFromMediaProfile(this.siblingNext.media.mediaProfile),
          params: {bookId: this.siblingNext.id.toString()},
          query: {context: this.context.origin, contextId: this.context.id, incognito: this.incognito.toString()},
        })
      }
    },
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
    toggleToolbars() {
      this.showToolbars = !this.showToolbars
    },
    toggleSettings() {
      this.showSettings = !this.showSettings
    },
    toggleTableOfContents() {
      this.showToc = !this.showToc
    },
    toggleHelp() {
      this.showHelp = !this.showHelp
    },
    keyPressed(e: KeyboardEvent) {
      this.shortcuts[e.key]?.execute(this)
    },
    clickThrough(e: MouseEvent) {
      if (e.detail === 1) {
        this.clickTimer = setTimeout(() => {
          this.toggleToolbars()
        }, 200)
      }
      if (e.detail === 2) {
        clearTimeout(this.clickTimer)
      }
    },
    async setup(bookId: string) {
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

      this.d2Reader = await D2Reader.load({
        url: new URL(bookManifestUrl(this.bookId)),
        userSettings: this.settings,
        storageType: 'memory',
        injectables: [
          // webpack will process the new URL (https://webpack.js.org/guides/asset-modules/#url-assets)
          // we use a different extension so that the css-loader rule is not used (see vue.config.js)
          {
            type: 'style',
            url: new URL('../styles/readium/ReadiumCSS-before.css.resource', import.meta.url).toString(),
            r2before: true,
          },
          {
            type: 'style',
            url: new URL('../styles/readium/ReadiumCSS-default.css.resource', import.meta.url).toString(),
            r2default: true,
          },
          {
            type: 'style',
            url: new URL('../styles/readium/ReadiumCSS-after.css.resource', import.meta.url).toString(),
            r2after: true,
          },
          {type: 'style', url: new URL('../styles/r2d2bc/popup.css.resource', import.meta.url).toString()},
          {type: 'style', url: new URL('../styles/r2d2bc/popover.css.resource', import.meta.url).toString()},
          {type: 'style', url: new URL('../styles/r2d2bc/style.css.resource', import.meta.url).toString()},
        ],
        requestConfig: {
          credentials: 'include',
        },
        attributes: {
          margin: 0, // subtract this from the iframe height, when setting the iframe minimum height
          navHeight: 10, // used for positioning the toolbox
          iframePaddingTop: 20, // top padding inside iframe
          bottomInfoHeight: 35, // #reader-info-bottom height
        },
        rights: {
          enableBookmarks: false,
          enableAnnotations: false,
          enableTTS: false,
          enableSearch: false,
          enableTimeline: false,
          enableDefinitions: false,
          enableContentProtection: false,
          enableMediaOverlays: false,
          enablePageBreaks: true,
          autoGeneratePositions: false,
          enableLineFocus: false,
          customKeyboardEvents: false,
          enableHistory: true,
          enableCitations: false,
          enableConsumption: false,
        },
        api: {
          updateCurrentLocation: this.updateCurrentLocation,
          keydownFallthrough: this.keyPressed,
          clickThrough: this.clickThrough,
        },
      })

      this.tocs.toc = this.d2Reader.tableOfContents
      this.tocs.landmarks = this.d2Reader.landmarks
      this.tocs.pageList = this.d2Reader.pageList

      if (this.alwaysFullscreen) this.enterFullscreen()

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
    historyBack() {
      this.d2Reader.historyBack()
    },
    historyForward() {
      this.d2Reader.historyForward()
    },
    updateCurrentLocation(location: Locator): Promise<Locator> {
      // handle history
      this.historyCanGoBack = this.d2Reader.historyCurrentIndex > 0
      this.historyCanGoForward = this.d2Reader.historyCurrentIndex < this.d2Reader.history?.length - 1

      this.currentLocation = location
      return new Promise(function (resolve, _) {
        resolve(location)
      })
    },
    appearanceClass(suffix?: string): string {
      let c = this.appearance.replace('readium-', '').replace('-on', '').replace('default', 'day')
      if (suffix) c += `-${suffix}`
      return c
    },
    goToEntry(tocEntry: TocEntry) {
      if (tocEntry.href !== undefined) {
        const url = new URL(tocEntry.href)
        let locations = {
          progression: 0,
        } as Locations
        let href = tocEntry.href
        if (url.hash) {
          locations = {
            fragment: url.hash.slice(1),
          }
          href = tocEntry.href.substring(0, tocEntry.href.indexOf('#'))
        }
        let locator = {
          href: href,
          locations: locations,
        }
        this.d2Reader.goTo(locator)
        this.showToc = false
      }
    },
    closeDialog() {
      if (this.showToc) {
        this.showToc = false
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
    closeBook() {
      this.$router.push(
        {
          name: this.book.oneshot ? 'browse-oneshot' : 'browse-book',
          params: {bookId: this.bookId.toString(), seriesId: this.book.seriesId},
          query: {context: this.context.origin, contextId: this.context.id},
        })
    },
    cycleViewingTheme() {
      const i = (this.appearances.map(x => x.value).indexOf(this.settings.appearance) + 1) % this.appearances.length
      const newValue = this.appearances[i]
      this.appearance = newValue.value
      const text = this.$t(newValue.text)
      this.sendNotification(`${this.$t('epubreader.settings.viewing_theme')}: ${text}`)
    },
    changeLayout(scroll: boolean) {
      this.verticalScroll = scroll
      const text = scroll ? this.$t('epubreader.settings.layout_scroll') : this.$t('epubreader.settings.layout_paginated')
      this.sendNotification(`${this.$t('epubreader.settings.layout')}: ${text}`)
    },
    cyclePagination() {
      if (this.verticalScroll) {
        this.columnCount = 'auto'
        this.changeLayout(false)
      } else {
        const i = (this.columnCounts.map(x => x.value).indexOf(this.settings.columnCount) + 1) % this.columnCounts.length
        const newValue = this.columnCounts[i]
        this.columnCount = newValue.value
        const text = this.$t(newValue.text)
        this.sendNotification(`${this.$t('epubreader.settings.column_count')}: ${text}`)
      }
    },
    changeFontSize(increase: boolean) {
      this.fontSize += increase ? 10 : -10
    },
    sendNotification(message: string, timeout: number = 4000) {
      this.notification.timeout = timeout
      this.notification.message = message
      this.notification.enabled = true
    },
  },
})
</script>
<style src="@d-i-t-a/reader/dist/reader.css"/>
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

.sepia-bg {
  background-color: #faf4e8;
}

.sepia {
  color: #faf4e8;
}

.day-bg {
  background-color: #fff;
}

.day {
  color: #fff;
}

.night-bg {
  background-color: #000000;
}

.night {
  color: #000000;
}

.scrolltab {
  overflow-y: scroll;
}

.scrolltab-content {
  max-height: calc(100vh - 48px);
}
</style>
