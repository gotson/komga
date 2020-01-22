<template>
  <div v-if="pages.length > 0" style="background: black; width: 100%; height: 100%">
    <!--  clickable zone: left  -->
    <div @click="rtl ? next() : prev()"
         class="left-quarter full-height"
         style="z-index: 1; position: absolute"
    />

    <!--  clickable zone: menu  -->
    <div @click="showMenu = true"
         class="center-half full-height"
         style="z-index: 1; position: absolute"
    />

    <!--  clickable zone: right  -->
    <div @click="rtl ? prev() : next()"
         class="right-quarter full-height"
         style="z-index: 1; position: absolute"
    />

    <!--  Carousel  -->
    <v-carousel v-model="carouselPage"
                :show-arrows="false"
                hide-delimiters
                :continuous="false"
                height="auto"
                touchless
                :reverse="rtl"
    >
      <!--  Carousel: pages  -->
      <v-carousel-item v-for="p in slidesRange"
                       :key="doublePages ? `db${p}` : `sp${p}`"
                       :eager="eagerLoad(p)"
      >
        <div :class="`d-flex flex-row${rtl ? '-reverse' : ''} justify-center`">
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
      </v-carousel-item>
    </v-carousel>

    <!--  Menu  -->
    <v-overlay :value="showMenu"
               opacity=".8"
    >
      <!--   Menu: left zone with arrow   -->
      <div class="fixed-position full-height left-quarter"
           style="display: flex; align-items: center; justify-content: center"
      >
        <v-icon size="8em">
          mdi-chevron-left
        </v-icon>
      </div>

      <!--   Menu: central zone   -->
      <div class="dashed-x fixed-position center-half full-height"
           @click.self="showMenu = false"
      >
        <div style="position: absolute; top: 1em; left: 1em">
          <v-btn @click="closeBook"
                 color="primary"
          >
            Close book
          </v-btn>

          <v-btn @click="showMenu = false; showThumbnailsExplorer = true"
                 color="primary"
                 class="ml-2"
          >
            <v-icon>mdi-view-grid</v-icon>
          </v-btn>
        </div>

        <v-btn icon
               @click="showMenu = false"
               absolute
               top
               right
        >
          <v-icon>mdi-close</v-icon>
        </v-btn>

        <v-container fluid
                     class="pa-6 pt-12"
                     style="border-bottom: 4px dashed"
        >
          <!--  Menu: number of pages  -->
          <v-row>
            <v-col class="text-center title">
              Page {{ currentPage }} of {{ pagesCount }}
            </v-col>
          </v-row>

          <!--  Menu: progress bar  -->
          <v-row>
            <v-col cols="12">
              <v-progress-linear :value="progress"
                                 height="20"
                                 background-color="white"
                                 color="secondary"
                                 rounded
              />
            </v-col>
          </v-row>

          <!--  Menu: go to page  -->
          <v-row align="baseline" justify="center">
            <v-col cols="auto">
              Go to page
            </v-col>
            <v-col cols="auto">
              <v-text-field
                v-model="goToPage"
                hide-details
                single-line
                type="number"
                @change="goTo"
                style="width: 4em"
              />
            </v-col>
          </v-row>

          <!--  Menu: page slider  -->
          <v-row align="baseline">
            <v-col cols="12">
              <v-slider
                v-model="goToPage"
                class="align-center"
                :max="pagesCount"
                min="1"
                hide-details
                @change="goTo"
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
            </v-col>
          </v-row>

          <!--  Menu: fit buttons  -->
          <v-row justify="center">
            <v-col cols="auto">
              <v-btn-toggle v-model="fitButtons" dense mandatory active-class="primary">
                <v-btn @click="setFit(ImageFit.WIDTH)">
                  Fit to width
                </v-btn>

                <v-btn @click="setFit(ImageFit.HEIGHT)">
                  Fit to height
                </v-btn>

                <v-btn @click="setFit(ImageFit.ORIGINAL)">
                  Original
                </v-btn>
              </v-btn-toggle>
            </v-col>
          </v-row>

          <!--  Menu: RTL buttons  -->
          <v-row justify="center">
            <v-col cols="auto">
              <v-btn-toggle v-model="rtlButtons" dense mandatory active-class="primary">
                <v-btn @click="setRtl(false)">
                  Left to right
                </v-btn>

                <v-btn @click="setRtl(true)">
                  Right to left
                </v-btn>
              </v-btn-toggle>
            </v-col>
          </v-row>

          <!--  Menu: double pages buttons  -->
          <v-row justify="center">
            <v-col cols="auto">
              <v-btn-toggle v-model="doublePagesButtons" dense mandatory active-class="primary">
                <v-btn @click="setDoublePages(false)">
                  Single page
                </v-btn>

                <v-btn @click="setDoublePages(true)">
                  Double pages
                </v-btn>
              </v-btn-toggle>
            </v-col>
          </v-row>

          <!--  Menu: keyboard shortcuts  -->
          <v-row>
            <v-col cols="auto">
              <div><kbd>←</kbd> / <kbd>⇞</kbd></div>
              <div><kbd>→</kbd> / <kbd>⇟</kbd></div>
              <div><kbd>home</kbd></div>
              <div><kbd>end</kbd></div>
              <div><kbd>space</kbd></div>
              <div><kbd>m</kbd></div>
              <div><kbd>t</kbd></div>
              <div><kbd>esc</kbd></div>
            </v-col>
            <v-col>
              <div v-if="!rtl">Previous page</div>
              <div v-else>Next page</div>
              <div v-if="!rtl">Next page</div>
              <div v-else>Previous page</div>
              <div>First page</div>
              <div>Last page</div>
              <div>Scroll down</div>
              <div>Show / hide menu</div>
              <div>Show / hide thumbnails</div>
              <div>Close book</div>
            </v-col>
          </v-row>
        </v-container>
      </div>

      <!--   Menu: right zone with arrow   -->
      <div class="fixed-position full-height right-quarter"
           style="display: flex; align-items: center; justify-content: center"
      >
        <v-icon size="8em">
          mdi-chevron-right
        </v-icon>
      </div>

    </v-overlay>

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

  </div>
</template>

<script lang="ts">
import { checkWebpFeature } from '@/functions/check-webp'
import { ImageFit } from '@/types/common'
import Vue from 'vue'

const cookieFit = 'webreader.fit'
const cookieRtl = 'webreader.rtl'
const cookieDoublePages = 'webreader.doublePages'

export default Vue.extend({
  name: 'BookReader',
  data: () => {
    return {
      ImageFit,
      baseURL: process.env.VUE_APP_KOMGA_API_URL ? process.env.VUE_APP_KOMGA_API_URL : window.location.origin,
      book: {} as BookDto,
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
      showMenu: false,
      fitButtons: 1,
      fit: ImageFit.HEIGHT,
      rtlButtons: 0,
      rtl: false,
      doublePages: false,
      doublePagesButtons: 0,
      showThumbnailsExplorer: false
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

    // restore options for RTL, fit, and double pages
    if (this.$cookies.isKey(cookieRtl)) {
      if (this.$cookies.get(cookieRtl) === 'true') {
        this.setRtl(true)
      }
    }
    if (this.$cookies.isKey(cookieFit)) {
      this.setFit(this.$cookies.get(cookieFit))
    }
    if (this.$cookies.isKey(cookieDoublePages)) {
      if (this.$cookies.get(cookieDoublePages) === 'true') {
        this.setDoublePages(true)
      }
    }
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
    maxHeight (): number | string {
      return this.fit === ImageFit.HEIGHT ? this.$vuetify.breakpoint.height : 'auto'
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
    }
  },
  methods: {
    keyPressed (e: KeyboardEvent) {
      switch (e.key) {
        case 'PageUp':
        case 'ArrowRight':
          this.rtl ? this.prev() : this.next()
          break
        case 'PageDown':
        case 'ArrowLeft':
          this.rtl ? this.next() : this.prev()
          break
        case 'Home':
          this.goToFirst()
          break
        case 'End':
          this.goToLast()
          break
        case 'm':
          this.showMenu = !this.showMenu
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
      let url = `${this.baseURL}/api/v1/books/${this.bookId}/pages/${page}`
      if (!this.supportedMediaTypes.includes(this.pages[page - 1].mediaType)) {
        url += `?convert=${this.convertTo}`
      }
      return url
    },
    getThumbnailUrl (page: number): string {
      return `${this.baseURL}/api/v1/books/${this.bookId}/pages/${page}/thumbnail`
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
    setRtl (rtl: boolean) {
      this.rtl = rtl
      this.rtlButtons = rtl ? 1 : 0
      this.$cookies.set(cookieRtl, rtl, Infinity)
    },
    setFit (fit: ImageFit) {
      this.fit = fit
      switch (fit) {
        case ImageFit.WIDTH:
          this.fitButtons = 0
          break
        case ImageFit.HEIGHT:
          this.fitButtons = 1
          break
        case ImageFit.ORIGINAL:
          this.fitButtons = 2
          break
      }
      this.$cookies.set(cookieFit, fit, Infinity)
    },
    setDoublePages (doublePages: boolean) {
      const current = this.currentPage
      this.doublePages = doublePages
      this.goTo(current)
      this.doublePagesButtons = doublePages ? 1 : 0
      this.$cookies.set(cookieDoublePages, doublePages, Infinity)
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
    maxWidth (p: number): number | string {
      if (this.fit !== ImageFit.WIDTH) {
        return 'auto'
      }
      if (this.doublePages && p !== 1 && p !== this.pagesCount) {
        return this.$vuetify.breakpoint.width / 2
      }
      return this.$vuetify.breakpoint.width
    }
  }
})
</script>

<style scoped>
.fixed-position {
  position: fixed;
}

.full-height {
  top: 0;
  height: 100%;
}

.left-quarter {
  left: 0;
  width: 20%;
}

.right-quarter {
  right: 0;
  width: 20%;
}

.center-half {
  left: 20%;
  width: 60%;
}

.dashed-x {
  border-left: 4px dashed;
  border-right: 4px dashed;
}
</style>
