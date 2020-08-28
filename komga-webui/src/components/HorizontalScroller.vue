<template>
  <div style="position: relative">
    <div style="min-height: 36px">
      <slot name="prepend"/>
    </div>
    <div style="position: absolute; top: 0; right: 0">
      <v-btn icon
             :disabled="!canScrollLeft"
             @click="doScroll('left')">
        <v-icon>mdi-chevron-left</v-icon>
      </v-btn>
      <v-btn icon
             :disabled="!canScrollRight"
             @click="doScroll('right')">
        <v-icon>mdi-chevron-right</v-icon>
      </v-btn>
    </div>

    <div class="scrolling-wrapper"
         :id="id"
         :ref="id"
         @scroll="computeScrollability"
         v-resize="computeScrollability"
    >
      <slot name="content" class="content"/>
    </div>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'HorizontalScroller',
  data: function () {
    const uniqueId = this.$_.uniqueId()
    return {
      id: uniqueId,
      canScrollLeft: false,
      canScrollRight: true,
      container: this.$refs[uniqueId] as HTMLElement,
      adjustment: 100,
    }
  },
  mounted () {
    this.container = this.$refs[this.id] as HTMLElement
    this.computeScrollability()
  },
  methods: {
    computeScrollability () {
      if (this.container !== undefined) {
        this.canScrollLeft = Math.round(this.container.scrollLeft) > 0
        this.canScrollRight = (Math.round(this.container.scrollLeft) + this.container.clientWidth) < this.container.scrollWidth
      }
    },
    doScroll (direction: string) {
      if (this.container !== undefined) {
        let target = Math.round(this.container.scrollLeft) + (this.container.clientWidth - this.adjustment)
        if (direction === 'left') {
          target = Math.round(this.container.scrollLeft) - (this.container.clientWidth - this.adjustment)
        }
        const scrollMax = this.container.clientWidth
        this.container.scrollTo({
          top: 0,
          left: target,
          behavior: 'smooth',
        })
      }
    },
  },
})
</script>

<style scoped>
.scrolling-wrapper {
  -webkit-overflow-scrolling: touch;
  display: flex;
  flex-wrap: nowrap;
  overflow-x: auto;
}

.scrolling-wrapper::-webkit-scrollbar {
  display: none;
}

.content {
  flex: 0 0 auto
}
</style>
