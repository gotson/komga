<template>
  <v-row justify="start" ref="content" v-resize="updateCardWidth" v-if="this.items">
      <v-skeleton-loader v-for="(item, i) in items"
                           :key="i"
                           justify-self="start"
                           :loading="item === null"
                           type="card, text"
                           class="ma-3 mx-2"
                           :data-index="i"
                           :width="cardWidth"
                           :height="cardHeight"
        >
          <slot name="card" v-bind:width="cardWidth" v-bind:item="item"></slot>
      </v-skeleton-loader>
  </v-row>
</template>

<script lang="ts">
import Vue from 'vue'
import { computeCardWidth } from '@/functions/grid-utilities'

export default Vue.extend({
  name: 'GridCards',
  data () {
    return {
      cardWidth: 150
    }
  },
  props: {
    items: {
      type: Array
    }
  },
  methods: {
    updateCardWidth () {
      const content = this.$refs.content as HTMLElement
      this.cardWidth = computeCardWidth(content.clientWidth, this.$vuetify.breakpoint.name)
    }
  },
  computed: {
    cardHeight (): number {
      return (this.cardWidth / 0.7071) + 116
    }
  }
})
</script>

<style scoped>

</style>
