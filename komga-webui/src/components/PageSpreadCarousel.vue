<template>
    <v-carousel :value.sync="pageCursor"
                :show-arrows="false"
                :continuous="false"
                :vertical="isVertical"
                :reverse="flipDirection"
                hide-delimiters
                touchless
                height="100%"
                v-if="spreads.spreads.length > 0"
    >
      <v-carousel-item
        class="full-height"
        v-for="(spread, i) in spreads.spreads"
        :key="i"
        :transition="transition"
        :reverse-transition="transition"
        :eager="eagerLoad(i)"
      >
        <page-spread-item :spread="spread" :reader="reader"></page-spread-item>
      </v-carousel-item>
    </v-carousel>
</template>

<script lang="ts">
import Vue from 'vue'
import PageSpreadItem from '@/components/PageSpreadItem.vue'

import { PageSpread, PageSpreads } from '@/functions/pages'
import { Reader, ReaderSettings, readSetting } from '@/functions/reader'
import { ReadingDirection } from '@/types/enum-books'

export default Vue.extend({
  components: { PageSpreadItem },
  name: 'PageSpreadCarousel',
  data: () => {
    return {
    }
  },
  props: {
    spreads: {
      type: [ Object as () => PageSpreads ],
      required: true,
    },
    reader: {
      type: [ Object as () => Reader ],
      required: true,
    },
    pageCursor: {
      type: [Number],
      required: true,
    },
  },
  watch: {
    pageCursor (val) {
      this.$emit('update:pageCursor', val)
    },
  },
  methods: {
    eagerLoad (p: number): boolean {
      return Math.abs(this.pageCursor - p) <= 2
    },
  },
  computed: {
    readingDirection: readSetting<ReadingDirection>(ReaderSettings.READ_DIR),
    animations: readSetting<boolean>(ReaderSettings.ANIMATIONS),
    flipDirection (): boolean {
      return this.readingDirection === ReadingDirection.RIGHT_TO_LEFT
    },
    isVertical (): boolean {
      return this.readingDirection === ReadingDirection.VERTICAL
    },
    transition () {
      return this.animations ? undefined : false
    },
  },
})
</script>

<style scoped>
  .full-height {
    height: 100%;
  }
</style>
