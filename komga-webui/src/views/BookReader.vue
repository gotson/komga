<template>
  <div v-if="pages.length > 0" style="background: black; width: 100%; height: 100%">
    <!--  clickable zone: previous page  -->
    <div @click="prev"
         class="left-quarter full-height"
         style="z-index: 1; position: absolute"
    />

    <!--  clickable zone: menu  -->
    <div @click="showMenu = true"
         class="center-half full-height"
         style="z-index: 1; position: absolute"
    />

    <!--  clickable zone: next page  -->
    <div @click="next"
         class="right-quarter full-height"
         style="z-index: 1; position: absolute"
    />

    <!--  Carousel  -->
    <slick ref="slick"
           :options="slickOptions"
           @afterChange="slickAfterChange"
    >
      <!--  Carousel: pages  -->
      <v-img v-for="p in pages"
             :key="p.number"
             :data-lazy="getPageUrl(p)"
             :src="getPageUrl(p)"
             lazy-src="../assets/loading.svg"
             :max-height="maxHeight"
             :max-width="$vuetify.breakpoint.width"
             :contain="true"
      />
    </slick>

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
        <v-btn @click="closeBook"
               color="primary"
               absolute top left
        >
          Close book
        </v-btn>

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
              Page {{ currentPage }} of {{ book.media.pagesCount }}
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
                :max="book.media.pagesCount"
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
              <v-btn-toggle v-model="fitButtons" dense mandatory>
                <v-btn @click="fitHeight = false" color="primary">
                  Fit to width
                </v-btn>

                <v-btn @click="fitHeight = true" color="primary">
                  Fit to height
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
              <div><kbd>esc</kbd></div>
            </v-col>
            <v-col>
              <div>Previous page</div>
              <div>Next page</div>
              <div>First page</div>
              <div>Last page</div>
              <div>Scroll down</div>
              <div>Show / hide menu</div>
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

  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import Slick from 'vue-slick'

export default Vue.extend({
  name: 'BookReader',
  components: { Slick },
  data: () => {
    return {
      baseURL: process.env.VUE_APP_KOMGA_API_URL ? process.env.VUE_APP_KOMGA_API_URL : window.location.origin,
      book: {} as BookDto,
      pages: [] as PageDto[],
      supportedMediaTypes: ['image/jpeg', 'image/png', 'image/gif'],
      convertTo: 'png',
      currentPage: 1,
      goToPage: 1,
      showMenu: false,
      fitButtons: 1,
      fitHeight: true,
      slickOptions: {
        infinite: false,
        arrows: false,
        variableWidth: false,
        adaptiveHeight: false,
        swipe: false,
        touchMove: false,
        cssEase: 'cubic-bezier(0.250, 0.100, 0.250, 1.000)',
        speed: 150,
        initialSlide: 0
      }
    }
  },
  async mounted () {
    window.addEventListener('keydown', this.keyPressed)
    this.setup(this.bookId, Number(this.$route.query.page))
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
      this.setup(Number(to.params.bookId), Number(to.query.page))
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
    canPrev (): boolean {
      return this.currentPage > 1
    },
    canNext (): boolean {
      return this.currentPage < this.book.media.pagesCount
    },
    progress (): number {
      return this.currentPage / this.book.media.pagesCount * 100
    },
    maxHeight (): number | undefined {
      return this.fitHeight ? this.$vuetify.breakpoint.height - 7 : undefined
    }
  },
  methods: {
    keyPressed (e: KeyboardEvent) {
      switch (e.key) {
        case 'PageUp':
        case 'ArrowRight':
          this.next()
          break
        case 'PageDown':
        case 'ArrowLeft':
          this.prev()
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
        case 'Escape':
          this.closeBook()
          break
      }
    },
    async setup (bookId: number, page: number) {
      this.book = await this.$komgaBooks.getBook(bookId)
      this.pages = await this.$komgaBooks.getBookPages(bookId)
      if (page >= 1 && page <= this.book.media.pagesCount) {
        this.currentPage = page
        this.slickOptions.initialSlide = page - 1
      } else {
        this.currentPage = 1
        this.updateRoute()
      }
    },
    getPageUrl (page: PageDto): string {
      let url = `${this.baseURL}/api/v1/books/${this.bookId}/pages/${page.number}`
      if (!this.supportedMediaTypes.includes(page.mediaType)) {
        url += `?convert=${this.convertTo}`
      }
      return url
    },
    prev () {
      if (this.canPrev) {
        (this.$refs.slick as any).prev()
        window.scrollTo(0, 0)
      }
    },
    next () {
      if (this.canNext) {
        (this.$refs.slick as any).next()
        window.scrollTo(0, 0)
      } else {
        this.showMenu = true
      }
    },
    goTo (page: number) {
      (this.$refs.slick as any).$el.slick.slickGoTo(page - 1, true)
    },
    goToFirst () {
      this.goToPage = 1
      this.goTo(this.goToPage)
    },
    goToLast () {
      this.goToPage = this.book.media.pagesCount
      this.goTo(this.goToPage)
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
    slickAfterChange (event: any, slick: any, currentSlide: any) {
      this.currentPage = currentSlide + 1
    }
  }
})
</script>

<style scoped>
@import "../../node_modules/slick-carousel/slick/slick.css";

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
