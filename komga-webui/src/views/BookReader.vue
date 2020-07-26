<template>
  <v-container class="ma-0 pa-0 full-height" fluid v-if="isReady"
               :style="`width: 100%; background-color: ${backgroundColor}`"
               v-touch="nav.vtouch()"
               :key="this.bookId"
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
                v-if="nav"
                hide-details
                thumb-label
                @change="changeSlider"
                v-model="goToPage"
                class="align-center"
                min="1"
                :max="realPageCount"
              >
                <template v-slot:prepend>
                  <v-icon @click="previousBook" class="">mdi-undo</v-icon>
                  <v-icon @click="goToFirst" class="mx-2">mdi-skip-previous</v-icon>
                  <v-label>
                    {{ realPageNumber }}
                  </v-label>
                </template>
                <template v-slot:append>
                  <v-label>
                    {{ realPageCount }}
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
    <div @click="nav.backward(true)"
         class="left-quarter full-height top"
         style="z-index: 1;"
    />

    <!--  clickable zone: menu  -->
    <div @click="toolbar = !toolbar"
         class="center-half full-height top"
         style="z-index: 1;"
    />

    <!--  clickable zone: right  -->
    <div @click="nav.forward(true)"
         class="right-quarter full-height top"
         style="z-index: 1;"
    />

    <div class="full-height">
    <page-spread-carousel
        v-if="isReady"
        :spreads="reader.spreads"
        :reader="this.reader"
        :page-cursor="pageCursor"
    ></page-spread-carousel>
    </div>

    <thumbnail-explorer-dialog
      v-if="isReady"
      v-model="showThumbnailsExplorer"
      @goToPage="this.nav.goTo"
      :bookId="bookId"
      :pagesCount="realPageCount"
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
              :items="formValues.backgroundColors"
              v-model="backgroundColor"
              label="Background color"
            >
            </settings-select>
          </v-list-item>

          <v-list-item>
            <settings-select
              :items="formValues.readingDirs"
              v-model="readingDirection"
              label="Reading Direction"
            />
          </v-list-item>

          <v-list-item>
            <settings-select
              :items="formValues.imageFits"
              v-model="imageFit"
              label="Scaling"
            />
          </v-list-item>
        </v-list>
      </v-container>
    </v-bottom-sheet>
    <v-snackbar
      v-if="reader && reader.loaded"
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
        <p v-if="siblingPrevious">Click or press previous again<br/>to move to the previous book.</p>
      </div>
    </v-snackbar>

    <v-snackbar
      v-if="reader && reader.loaded"
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
        <p v-if="siblingNext">Click or press next again<br/>to move to the next book.</p>
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
      <div>Reading direction has been set to <span class="font-weight-bold">{{ ReadingDirection.toString(readingDirection) }}</span>.
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
import PageSpreadCarousel from '@/components/PageSpreadCarousel.vue'
import ThumbnailExplorerDialog from '@/components/dialogs/ThumbnailExplorerDialog.vue'
import ShortcutHelpMenu from '@/components/menus/ShortcutHelpMenu.vue'
import {
  ReadingDirection,
  ImageFit,
  READING_DIR_ITEMS,
  IMAGE_FIT_ITEMS,
  BACKGROUND_COLOR_ITEMS,
} from '@/types/enum-books'
import { executeShortcut } from '@/functions/shortcuts'

import { Navigator, Reader, computedSetting, ReaderSettings } from '@/functions/reader'
import Vue from 'vue'

type _reader = Reader | undefined

export default Vue.extend({
  name: 'BookReader',
  components: { SettingsSwitch, SettingsSelect, ShortcutHelpMenu, ThumbnailExplorerDialog, PageSpreadCarousel },
  data: () => {
    return {
      reader: undefined as _reader,
      goToPage: 1,
      jumpToNextBook: false,
      jumpToPreviousBook: false,
      jumpConfirmationDelay: 3000,
      snackReadingDirection: false,
      showThumbnailsExplorer: false,
      toolbar: false,
      menu: false,
      dialogGoto: false,
      notification: {
        enabled: false,
        message: '',
        timeout: 4000,
      },
      formValues: {
        readingDirs: READING_DIR_ITEMS,
        imageFits: IMAGE_FIT_ITEMS,
        backgroundColors: BACKGROUND_COLOR_ITEMS,
      },
      ReadingDirection,
    }
  },
  async mounted () {
    window.addEventListener('keydown', this.keyPressed)
    await this.setup(this.bookId, Number(this.$route.query.page))
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
  watch: {
    realPageNumber (val) {
      this.updateRoute()
      this.goToPage = val
      this.reader?.markProgress(val)
    },
    async bookId (val) {
      await this.setup(val, 0)
    },
  },
  computed: {
    isReady (): boolean {
      return !!this.reader && !!this.reader.navigator && !!this.reader?.spreads && this.reader?.spreads?.spreads?.length!! > 0
    },
    nav (): Navigator {
      return this.reader?.navigator!
    },
    pageCursor: {
      get: function (): number {
        return this.nav?.pageCursor || NaN
      },
      set: function (v: number): void {
        this.nav.pageCursor = v
      },
    },
    progress (): number {
      return this.nav.progress
    },
    realPageNumber (): number {
      if (!this.isReady) {
        return 0
      }
      return this.nav.realPageNumber
    },
    realPageCount (): number {
      return this.reader?.pages.length!! + 1
    },
    bookTitle (): string {
      return this.reader?.bookTitle()!!
    },
    flipDirection (): boolean {
      return this.readingDirection === ReadingDirection.RIGHT_TO_LEFT
    },
    vertical (): boolean {
      return this.readingDirection === ReadingDirection.VERTICAL
    },
    siblingPrevious (): BookDto {
      return this.reader?.siblingPrev!!
    },
    siblingNext (): BookDto {
      return this.reader?.siblingNext!!
    },
    imageFit: computedSetting<ImageFit>(ReaderSettings.FIT),
    backgroundColor: computedSetting<string>(ReaderSettings.BG_COLOR),
    readingDirection: computedSetting<ReadingDirection>(ReaderSettings.READ_DIR),
    doublePages: computedSetting<boolean>(ReaderSettings.DOUBLE_PAGES),
    swipe: computedSetting<boolean>(ReaderSettings.SWIPE),
    animations: computedSetting<boolean>(ReaderSettings.ANIMATIONS),
  },
  methods: {
    keyPressed (e: KeyboardEvent) {
      executeShortcut(this, e)
    },
    async setup (bookId: string, page: number) {
      this.reader = new Reader(this.$komgaBooks, this.$komgaSeries, this.$cookies)
      await this.reader.setup(this.bookId)
      document.title = `Komga - ${this.reader.bookTitle()}`
      this.snackReadingDirection = this.reader?.coerceReadingDirection()
      const p = this.reader.determinePage(page)
      this.goTo(p)
    },
    // prev () {
    //   if (this.canPrev) {
    //     this.carouselPage--
    //     window.scrollTo(0, 0)
    //   } else {
    //     if (this.jumpToPreviousBook) {
    //       this.previousBook()
    //     } else {
    //       this.jumpToPreviousBook = true
    //     }
    //   }
    // },
    // next () {
    //   if (this.canNext) {
    //     this.carouselPage++
    //     window.scrollTo(0, 0)
    //   } else {
    //     if (this.jumpToNextBook) {
    //       this.nextBook()
    //     } else {
    //       this.jumpToNextBook = true
    //     }
    //   }
    // },
    nextBook () {
      if (this.siblingNext) {
        this.jumpToNextBook = false
        this.nav.goToBook(this.$router, this.siblingNext!!)
      } else {
        this.closeBook()
      }
    },
    previousBook () {
      this.jumpToPreviousBook = false
      if (this.siblingPrevious) {
        this.nav.goToBook(this.$router, this.siblingPrevious!!)
      }
    },
    changeSlider (page: number) {
      this.goTo(page)
    },
    goTo (page: number) {
      this.nav.goTo(page)
    },
    goToFirst () {
      this.nav.goToFirst()
    },
    goToLast () {
      this.nav.goToLast()
    },
    updateRoute () {
      if (this.isReady) {
        this.$router.replace({
          name: this.$route.name,
          params: { bookId: this.$route.params.bookId },
          query: {
            page: this.realPageNumber.toString(),
          },
        })
      }
    },
    closeBook () {
      this.$router.push({ name: 'browse-book', params: { bookId: this.bookId.toString() } })
    },
    changeReadingDir (dir: ReadingDirection) {
      this.reader?.set(ReaderSettings.READ_DIR, dir)
      this.sendNotification(`Changing Reading Direction to: ${ReadingDirection.toString(dir)}`)
    },
    cycleScale () {
      let fit: ImageFit = this.reader?.get<ImageFit>(ReaderSettings.FIT)!!
      let next = ImageFit.next(fit)
      this.reader?.set(ReaderSettings.FIT, next)
      this.sendNotification(`Cycling Scale: ${ImageFit.toString(next)}`)
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
