<template>
  <v-menu offset-y>
    <template v-slot:activator="{on}">
      <v-btn icon v-on="on">
        <v-icon>mdi-view-grid-plus</v-icon>
      </v-btn>
    </template>
    <v-list :dark="dark">
      <v-list-item-group v-model="selection">

        <v-list-item v-for="(item, index) in items"
                     :key="index"
                     @click="setPageSize(item)"
        >
          <v-list-item-title>{{ item }}</v-list-item-title>
        </v-list-item>
      </v-list-item-group>
    </v-list>
  </v-menu>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'PageSizeSelect',
  data: () => {
    return {
      selection: 0,
    }
  },
  props: {
    items: {
      type: Array,
      default: () => [20, 50, 100, 200, 500],
    },
    value: {
      type: Number,
      required: true,
    },
    dark: {
      type: Boolean,
      default: false,
    },
  },
  watch: {
    value: {
      handler(val) {
        this.selection = this.items.findIndex(x => x === val)
      },
      immediate: true,
    },
  },
  methods: {
    setPageSize (size: number) {
      this.$emit('input', size)
    },
  },
})
</script>

<style scoped>

</style>
