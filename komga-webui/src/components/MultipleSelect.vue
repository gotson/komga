<template>
  <div>
    <slot name="selectedItems" v-bind:selectedItems="selectedItems"></slot>
    <v-item-group multiple v-model="selectedItems">
      <v-container fluid class="px-6">
        <slot name="item" v-bind:pre="preSelected" v-bind:items="items" v-if="this.items">
        </slot>
        <slot name="empty" v-else>
        </slot>
      </v-container>
    </v-item-group>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'MultipleSelect',
  data () {
    return {
      selectedItems: []
    }
  },
  props: {
    items: {
      type: Array
    },
    selected: {
      type: Array
    }
  },
  watch: {
    selected: {
      handler (after) {
        this.selectedItems = after
      },
      immediate: true
    },
    selectedItems (after) {
      this.$emit('update:selected', after)
    }
  },
  computed: {
    preSelected (): boolean {
      return this.selectedItems.length > 0
    }
  }
})
</script>

<style scoped>

</style>
