<template>
  <div
    v-touch="{
               left: () => {if(swipe) {turnRight()}},
               right: () => {if(swipe) {turnLeft()}},
               up: () => {if(swipe) {verticalNext()}},
               down: () => {if(swipe) {verticalPrev()}}
             }"
  >
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
          <div :class="`d-flex flex-row${flipDirection ? '-reverse' : ''} justify-center px-0 mx-0`">
            <img v-for="(page, j) in spread"
                 :alt="`Page ${page.number}`"
                 :key="`spread${i}-${j}`"
                 :src="page.url"
                 :class="imgClass(spread)"
                 class="img-fit-all"
            />
          </div>
        </div>
      </v-carousel-item>
    </v-carousel>

    <!--  clickable zone: left  -->
    <div v-if="!vertical"
         @click="turnLeft()"
         class="left-quarter"
         style="z-index: 1;"
    />

    <!--  clickable zone: right  -->
    <div v-if="!vertical"
         @click="turnRight()"
         class="right-quarter"
         style="z-index: 1;"
    />

    <!--  clickable zone: top  -->
    <div v-if="vertical"
         @click="verticalPrev()"
         class="top-quarter"
         style="z-index: 1;"
    />

    <!--  clickable zone: bottom  -->
    <div v-if="vertical"
         @click="verticalNext()"
         class="bottom-quarter"
         style="z-index: 1;"
    />

    <!--  clickable zone: menu  -->
    <div @click="centerClick()"
         :class="`${vertical ? 'center-vertical' : 'center-horizontal'}`"
         style="z-index: 1;"
    />
  </div>
</template>

<script lang="ts">
import { isPageLandscape } from '@/functions/page'
import Vue from 'vue'
import { ReadingDirection } from '@/types/enum-books'
import { PagedReaderLayout, ScaleType } from '@/types/enum-reader'
import { shortcutsLTR, shortcutsRTL, shortcutsVertical } from '@/functions/shortcuts/paged-reader'

export default Vue.extend({
  name: 'PagedReader',
  data: () => {
    return {
      carouselPage: 0,
      spreads: [] as PageDtoWithUrl[][],
    }
  },
  props: {
    pages: {
      type: Array as () => PageDtoWithUrl[],
      required: true,
    },
    page: {
      type: Number,
      required: true,
    },
    pageLayout: {
      type: String as () => PagedReaderLayout,
      required: true,
    },
    animations: {
      type: Boolean,
      required: true,
    },
    swipe: {
      type: Boolean,
      required: true,
    },
    readingDirection: {
      type: String as () => ReadingDirection,
      required: true,
    },
    scale: {
      type: String as () => ScaleType,
      required: true,
    },
  },
  watch: {
    pages: {
      handler () {
        this.spreads = this.buildSpreads()
      },
      immediate: true,
    },
    currentPage (val) {
      this.$emit('update:page', val)
    },
    page (val) {
      this.carouselPage = this.toSpreadIndex(val)
    },
    pageLayout: {
      handler () {
        const current = this.page
        this.spreads = this.buildSpreads()
        this.carouselPage = this.toSpreadIndex(current)
      },
      immediate: true,
    },
  },
  created () {
    window.addEventListener('keydown', this.keyPressed)
  },
  destroyed () {
    window.removeEventListener('keydown', this.keyPressed)
  },
  computed: {
    shortcuts (): any {
      const shortcuts = []
      switch (this.readingDirection) {
        case ReadingDirection.LEFT_TO_RIGHT:
          shortcuts.push(...shortcutsLTR)
          break
        case ReadingDirection.RIGHT_TO_LEFT:
          shortcuts.push(...shortcutsRTL)
          break
        case ReadingDirection.VERTICAL:
          shortcuts.push(...shortcutsVertical)
          break
      }
      return this.$_.keyBy(shortcuts, x => x.key)
    },
    flipDirection (): boolean {
      return this.readingDirection === ReadingDirection.RIGHT_TO_LEFT
    },
    vertical (): boolean {
      return this.readingDirection === ReadingDirection.VERTICAL
    },
    currentSlide (): number {
      return this.carouselPage + 1
    },
    currentPage (): number {
      if (this.carouselPage >= 0 && this.carouselPage < this.spreads.length && this.spreads.length > 0) {
        return this.spreads[this.carouselPage][0].number
      }
      return 1
    },
    slidesCount (): number {
      return this.spreads.length
    },
    canPrev (): boolean {
      return this.currentSlide > 1
    },
    canNext (): boolean {
      return this.currentSlide < this.slidesCount
    },
    isDoublePages (): boolean {
      return this.pageLayout === PagedReaderLayout.DOUBLE_PAGES || this.pageLayout === PagedReaderLayout.DOUBLE_NO_COVER
    },
  },
  methods: {
    keyPressed (e: KeyboardEvent) {
      this.shortcuts[e.key]?.execute(this)
    },
    buildSpreads (): PageDtoWithUrl[][] {
      if (this.pages.length === 0) return []
      if (this.isDoublePages) {
        const spreads = []
        let pages: PageDtoWithUrl[]
        if (this.pageLayout === PagedReaderLayout.DOUBLE_PAGES) {
          spreads.push([this.pages[0]])
          pages = this.$_.drop(this.$_.dropRight(this.pages))
        } else {
          pages = this.$_.cloneDeep(this.pages)
        }
        while (pages.length > 0) {
          const p = pages.shift() as PageDtoWithUrl
          if (isPageLandscape(p)) {
            spreads.push([p])
          } else {
            if (pages.length > 0) {
              const p2 = pages.shift() as PageDtoWithUrl
              if (isPageLandscape(p2)) {
                spreads.push([p])
                spreads.push([p2])
              } else {
                spreads.push([p, p2])
              }
            } else {
              spreads.push([p])
            }
          }
        }
        if (this.pageLayout === PagedReaderLayout.DOUBLE_PAGES) {
          spreads.push([this.pages[this.pages.length - 1]])
        }
        return spreads
      } else {
        return this.pages.map(p => [p])
      }
    },
    imgClass (spread: PageDtoWithUrl[]): string {
      const double = spread.length > 1
      switch (this.scale) {
        case ScaleType.WIDTH:
          return double ? 'img-double-fit-width' : 'img-fit-width'
        case ScaleType.HEIGHT:
          return 'img-fit-height'
        case ScaleType.SCREEN:
          return double ? 'img-double-fit-screen' : 'img-fit-screen'
        default:
          return 'img-fit-original'
      }
    },
    eagerLoad (spreadIndex: number): boolean {
      return Math.abs(this.carouselPage - spreadIndex) <= 2
    },
    centerClick () {
      this.$emit('menu')
    },
    turnRight () {
      if (!this.vertical)
        this.flipDirection ? this.prev() : this.next()
    },
    turnLeft () {
      if (!this.vertical)
        this.flipDirection ? this.next() : this.prev()
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
        this.$emit('jump-previous')
      }
    },
    next () {
      if (this.canNext) {
        this.carouselPage++
        window.scrollTo(0, 0)
      } else {
        this.$emit('jump-next')
      }
    },
    toSpreadIndex (i: number): number {
      if (this.spreads.length > 0) {
        if (this.isDoublePages) {
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
  },
})
</script>
<style scoped>
.full-height {
  height: 100%;
}

.left-quarter {
  top: 0;
  left: 0;
  width: 25%;
  height: 100%;
  position: absolute;
}

.right-quarter {
  top: 0;
  right: 0;
  width: 25%;
  height: 100%;
  position: absolute;
}

.top-quarter {
  top: 0;
  height: 25%;
  width: 100%;
  position: absolute;
}

.bottom-quarter {
  bottom: 0;
  height: 25%;
  width: 100%;
  position: absolute;
}

.center-horizontal {
  top: 0;
  left: 25%;
  width: 50%;
  height: 100%;
  position: absolute;
}

.center-vertical {
  top: 25%;
  height: 50%;
  width: 100%;
  position: absolute;
}

.img-fit-all {
  object-fit: contain;
  object-position: center;
}

.img-fit-width {
  width: 100vw;
  min-height: 100vh;
  align-self: flex-start;
}

.img-double-fit-width {
  width: 50vw;
  min-height: 100vh;
  align-self: flex-start;
}

.img-fit-original {
  width: auto;
  height: auto;
}

.img-fit-height {
  min-height: 100vh;
  height: 100vh;
}

.img-fit-screen {
  width: 100vw;
  height: 100vh;
}

.img-double-fit-screen {
  max-width: 50vw;
  height: 100vh;
}
</style>
