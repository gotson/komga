<template>
  <v-container class="ma-0 pa-0 full-height" fluid v-if="pages.length > 0" style="width: 100%;"
       v-touch="{
       left: () => next(),
      right: () => prev(),
      }"
  >
    <div>
    <v-toolbar
      dense elevation="1"
      :class="`settings ${toolbar ? '' : 'd-none'}`"
      width="100%"
      absolute

    >
      <v-btn
        icon
        @click="closeBook"
      >
        <v-icon>mdi-arrow-left</v-icon>
      </v-btn>
      <v-toolbar-title> {{ this.bookTitle }}</v-toolbar-title>

      <v-spacer></v-spacer>

      <v-dialog
        v-model="menu"
        :close-on-content-click="false"
        fullscreen
        hide-overlay
        bottom
        transition="dialog-bottom-transition"
      >
        <template v-slot:activator="{ on }">
          <v-btn
            icon
            v-on="on"
          >
            <v-icon>mdi-settings</v-icon>
          </v-btn>
        </template>

        <v-toolbar dark color="primary">
          <v-btn icon dark @click="menu = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
          <v-toolbar-title>Reader Settings</v-toolbar-title>
        </v-toolbar>

        <v-layout class="full-height d-flex flex-column justify-center">
          <v-flex>
            <v-list class="full-height">
              <v-list-item class="">
                <settings-switch v-model="doublePages" :label="`${ doublePages ? 'Double Pages' : 'Single Page'}`"></settings-switch>
              </v-list-item>
              <v-list-item class="">
                <settings-switch v-model="flipDirection" :label="`${!flipDirection ? 'Right to left' : 'Left to right'}`"></settings-switch>
              </v-list-item>
              <v-list-item class="">
                <settings-combo
                  :items="pageLayout.imageFits"
                  v-model="imageFit"
                  label="Scaling"
                >
                  <template slot="item" slot-scope="data">
                    <div class="text-capitalize">
                      {{ imageFitDisplay(data.item) }}
                    </div>
                  </template>
                  <template slot="selection" slot-scope="data">
                    <div class="text-capitalize">
                      {{ imageFitDisplay(data.item)  }}
                    </div>
                  </template>
                </settings-combo>
              </v-list-item>
            </v-list>
          </v-flex>
        </v-layout>
      </v-dialog>

    </v-toolbar>
    <v-bottom-navigation
      dense
      elevation="1"
      :class="`settings ${toolbar ? '' : 'd-none'}`"
      absolute
      horizontal
    >
      <v-layout class="flex-column justify-center">
        <v-flex class="text-center font-weight-light">
          {{ currentPage }}/{{ pagesCount }}
        </v-flex>
         <!--  Menu: page slider  -->
        <v-flex class="">
          <v-slider
            v-model="goToPage"
            class="align-center"
            :max="pagesCount"
            min="1"
            hide-details
            @change="goTo"
            thumb-label
          >
            <template v-slot:prepend>
              <v-btn icon @click="goToFirst">
                <v-icon>mdi-arrow-collapse-left</v-icon>
              </v-btn>
            </template>
            <template v-slot:append>
              <v-btn icon @click="goToLast">
                <v-icon>mdi-arrow-collapse-right</v-icon>
              </v-btn>
            </template>
          </v-slider>
        </v-flex>
      </v-layout>

    </v-bottom-navigation>
    </div>

    <!--  clickable zone: left  -->
    <div @click="flipDirection ? next() : prev()"
         class="left-quarter full-height top"
         style="z-index: 1;"
    />

    <!--  clickable zone: menu  -->
    <div @click="toolbar = !toolbar"
         class="center-half full-height top"
         style="z-index: 1;"
    />

    <!--  clickable zone: right  -->
    <div @click="flipDirection ? prev() : next()"
         class="right-quarter full-height top"
         style="z-index: 1;"
    />

    <div class="full-height">
    <!--  Carousel  -->
    <v-carousel v-model="carouselPage"
                :show-arrows="false"
                hide-delimiters
                :continuous="false"
                touchless
                :reverse="flipDirection"
                height="100%"
    >
      <!--  Carousel: pages  -->
      <v-carousel-item v-for="p in slidesRange"
                       :key="doublePages ? `db${p}` : `sp${p}`"
                       :eager="eagerLoad(p)"
                       class="full-height"
      >
        <div class="full-height d-flex flex-column justify-center reader-background">
          <div :class="`d-flex flex-row${flipDirection ? '-reverse' : ''} justify-center px-0 mx-0` " >
            <img :src="getPageUrl(p)"
                 :height="maxHeight"
                 :width="maxWidth(p)"
            />
            <img v-if="doublePages && p !== 1 && p !== pagesCount && p+1 !== pagesCount"
                 :src="getPageUrl(p+1)"
                 :height="maxHeight"
                 :width="maxWidth(p+1)"
            />
          </div>
        </div>
      </v-carousel-item>
    </v-carousel>
    </div>

    <v-dialog v-model="showThumbnailsExplorer" scrollable>
      <v-card :max-height="$vuetify.breakpoint.height * .9"
              dark
      >
        <v-card-text>
          <v-container fluid>
            <v-row>
              <div v-for="p in pagesCount"
                   :key="p"
                   style="min-height: 220px; max-width: 140px"
                   class="mb-2"
              >
                <v-img
                  :src="getThumbnailUrl(p)"
                  lazy-src="../assets/cover.svg"
                  aspect-ratio="0.7071"
                  :contain="true"
                  max-height="200"
                  max-width="140"
                  class="ma-2"
                  @click="showThumbnailsExplorer = false; goTo(p)"
                  style="cursor: pointer"
                />
                <div class="white--text text-center font-weight-bold">{{p}}</div>
              </div>
            </v-row>
          </v-container>
        </v-card-text>
      </v-card>
    </v-dialog>

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
  </v-container>
</template>

<script lang="ts">
import SettingsSwitch from '@/components/SettingsSwitch.vue'
import SettingsCombo from '@/components/SettingsCombo.vue'

import { checkWebpFeature } from '@/functions/check-webp'
import { bookPageThumbnailUrl, bookPageUrl } from '@/functions/urls'
import { ImageFit } from '@/types/common'
import Vue from 'vue'
import { getBookTitleCompact } from '@/functions/book-title'

const cookieFit = 'webreader.fit'
const cookieRtl = 'webreader.flipDirection'
const cookieDoublePages = 'webreader.doublePages'

export default Vue.extend({
  name: 'BookReader',
  components: { SettingsSwitch, SettingsCombo },
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
      pages: [] as PageDto[],
      supportedMediaTypes: ['image/jpeg', 'image/png', 'image/gif'],
      convertTo: 'jpeg',
      carouselPage: 0,
      goToPage: 1,
      showThumbnailsExplorer: false,
      toolbar: true,
      menu: false,
      pageLayout: {
        doublePages: false,
        imageFits: Object.values(ImageFit),
        fit: ImageFit.HEIGHT,
        flipDirection: false
      }
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
    this.setup(this.bookId, Number(this.$route.query.page))

    this.loadFromCookie(cookieRtl, (v) => { this.flipDirection = (v === 'true') })
    this.loadFromCookie(cookieDoublePages, (v) => { this.doublePages = (v === 'true') })
    this.loadFromCookie(cookieFit, (v) => { if (v) { this.imageFit = v } })
  },
  destroyed () {
    window.removeEventListener('keydown', this.keyPressed)
  },
  props: {
    bookId: {
      type: Number,
      required: true
    }
  },
  async beforeRouteUpdate (to, from, next) {
    if (to.params.bookId !== from.params.bookId) {
      // route update means going to previous/next book, in this case we start from first page
      this.setup(Number(to.params.bookId), 1)
    }
    next()
  },
  watch: {
    currentPage (val) {
      this.updateRoute()
      this.goToPage = val
    },
    async book (val) {
      if (this.$_.has(val, 'name')) {
        this.series = await this.$komgaSeries.getOneSeries(val.seriesId)
        document.title = `Komga - ${getBookTitleCompact(val.name, this.series.name)}`
      }
    }
  },
  computed: {
    currentSlide (): number {
      return this.carouselPage + 1
    },
    currentPage (): number {
      return this.doublePages ? this.toSinglePages(this.currentSlide) : this.currentSlide
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
    slidesRange (): number[] {
      if (!this.doublePages) {
        return this.$_.range(1, this.pagesCount + 1)
      }
      // for double pages the first and last pages are shown as single, while others are doubled
      const ret = this.$_.range(2, this.pagesCount, 2)
      ret.unshift(1)
      ret.push(this.pagesCount)
      return ret
    },
    slidesCount (): number {
      return this.slidesRange.length
    },
    pagesCount (): number {
      return this.pages.length
    },
    bookTitle (): string {
      return getBookTitleCompact(this.book.name, this.series.name)
    },
    flipDirection: {
      get: function (): boolean {
        return this.pageLayout.flipDirection
      },
      set: function (flipDirection: boolean): void {
        this.pageLayout.flipDirection = flipDirection
        this.$cookies.set(cookieRtl, flipDirection, Infinity)
      }
    },
    imageFit: {
      get: function (): ImageFit {
        return this.pageLayout.fit
      },
      set: function (fit: ImageFit): void {
        this.pageLayout.fit = fit
        this.$cookies.set(cookieFit, fit, Infinity)
      }
    },
    doublePages: {
      get: function (): boolean {
        return this.pageLayout.doublePages
      },
      set: function (doublePages: boolean): void {
        const current = this.currentPage
        this.pageLayout.doublePages = doublePages
        this.goTo(current)
        this.$cookies.set(cookieDoublePages, doublePages, Infinity)
      }
    }
  },
  methods: {
    swipe (direction: string) {
      alert(direction)
    },
    keyPressed (e: KeyboardEvent) {
      switch (e.key) {
        case 'PageUp':
        case 'ArrowRight':
          this.pageLayout.flipDirection ? this.prev() : this.next()
          break
        case 'PageDown':
        case 'ArrowLeft':
          this.pageLayout.flipDirection ? this.next() : this.prev()
          break
        case 'Home':
          this.goToFirst()
          break
        case 'End':
          this.goToLast()
          break
        case 'm':
          this.toolbar = !this.toolbar
          break
        case 't':
          this.showThumbnailsExplorer = !this.showThumbnailsExplorer
          break
        case 'Escape':
          this.closeBook()
          break
      }
    },
    async setup (bookId: number, page: number) {
      this.book = await this.$komgaBooks.getBook(bookId)
      this.pages = await this.$komgaBooks.getBookPages(bookId)
      if (page >= 1 && page <= this.pagesCount) {
        this.goTo(page)
      } else {
        this.goToFirst()
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
    getPageUrl (page: number): string {
      if (!this.supportedMediaTypes.includes(this.pages[page - 1].mediaType)) {
        return bookPageUrl(this.bookId, page, this.convertTo)
      } else {
        return bookPageUrl(this.bookId, page)
      }
    },
    getThumbnailUrl (page: number): string {
      return bookPageThumbnailUrl(this.bookId, page)
    },
    prev () {
      if (this.canPrev) {
        this.carouselPage--
        window.scrollTo(0, 0)
      } else {
        if (this.jumpToPreviousBook) {
          if (!this.$_.isEmpty(this.siblingPrevious)) {
            this.jumpToPreviousBook = false
            this.$router.push({ name: 'read-book', params: { bookId: this.siblingPrevious.id.toString() } })
          }
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
          if (this.$_.isEmpty(this.siblingNext)) {
            this.closeBook()
          } else {
            this.jumpToNextBook = false
            this.$router.push({ name: 'read-book', params: { bookId: this.siblingNext.id.toString() } })
          }
        } else {
          this.jumpToNextBook = true
        }
      }
    },
    goTo (page: number) {
      this.carouselPage = this.doublePages ? this.toDoublePages(page) - 1 : page - 1
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
          page: this.currentPage.toString()
        }
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
    toDoublePages (i: number): number {
      let ret = Math.floor(i / 2) + 1
      if (i === this.pagesCount && this.pagesCount % 2 === 1) {
        ret++
      }
      return ret
    },
    eagerLoad (p: number): boolean {
      return Math.abs(this.currentPage - p) <= 2
    },
    maxWidth (p: number): number | null {
      if (this.imageFit !== ImageFit.WIDTH) {
        return null
      }
      if (this.doublePages && p !== 1 && p !== this.pagesCount) {
        return this.$vuetify.breakpoint.width / 2
      }
      return this.$vuetify.breakpoint.width
    },
    imageFitDisplay (fit: ImageFit): string {
      let display = {
        [ImageFit.HEIGHT]: 'fit to height',
        [ImageFit.WIDTH]: 'fit to width',
        [ImageFit.ORIGINAL]: 'original'
      }
      return display[fit]
    },
    loadFromCookie (cookieKey: string, setter: (value: any) => void): void {
      if (this.$cookies.isKey(cookieKey)) {
        let value = this.$cookies.get(cookieKey)
        setter(value)
      }
    }
  }
})
</script>

<style scoped>

.reader-background {
     background-color: white; /* TODO add a setting for this, some books might not be white */
}

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
