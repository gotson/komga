<template>
  <div v-if="spread && spread.pages.length > 0" class="full-height d-flex flex-column justify-center" >
    <div class="d-flex justify-center px-0 mx-0" :class="flipClass">
        <img v-for="page in spread.pages"
           :src="page.url"
           :key="page.number"
           :height="maxHeight"
           :width="maxWidth"
           :class="imgClass"
        />
    </div>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import { PageSpread } from '@/functions/pages'
import { Optional, Reader, ReaderSettings, readSetting } from '@/functions/reader'
import { ImageFit, ReadingDirection } from '@/types/enum-books'

export default Vue.extend({
  name: 'PageSpreadItem',
  props: {
    spread: {
      type: [Object as () => PageSpread],
      required: true,
    },
    reader: {
      type: [ Object as () => Reader ],
      required: true,
    },

  },
  computed: {
    imageFit: readSetting<ImageFit>(ReaderSettings.FIT),
    readingDirection: readSetting<ReadingDirection>(ReaderSettings.READ_DIR),
    flipDirection (): boolean {
      return this.readingDirection === ReadingDirection.RIGHT_TO_LEFT
    },
    flipClass (): object {
      return {
        [this.flipDirection ? 'flex-row-reverse' : 'flex-row']: true,
      }
    },
    imgClass () {
      return {
        'imagefit_width': this.imageFit === ImageFit.WIDTH,
      }
    },
    maxHeight (): Optional<number> {
      return this.imageFit === ImageFit.HEIGHT ? this.$vuetify.breakpoint.height : undefined
    },
    maxWidth (): Optional<number> {
      if (this.imageFit !== ImageFit.WIDTH) {
        return undefined
      }
      return this.$vuetify.breakpoint.width / this.spread.pages.length
    },
  },

})
</script>

<style scoped>
  .imagefit_width {
    height: inherit;
  }
  .full-height {
    height: 100%;
  }
</style>
