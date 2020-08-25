<template>
  <v-list dense>
    <v-list-item v-for="(item, index) in sortOptions"
                 :key="index"
                 @click="setSort(item)"
    >
      <v-list-item-icon>
        <v-icon color="secondary" v-if="item.key === sortActive.key && sortActive.order === 'asc'">
          mdi-chevron-up
        </v-icon>
        <v-icon color="secondary" v-if="item.key === sortActive.key && sortActive.order === 'desc'">
          mdi-chevron-down
        </v-icon>
      </v-list-item-icon>
      <v-list-item-title>{{ item.name }}</v-list-item-title>
    </v-list-item>
  </v-list>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'SortList',
  props: {
    sortOptions: {
      type: Array,
      required: true,
    },
    sortDefault: {
      type: Object,
      required: true,
    },
    sortActive: {
      type: Object,
      required: true,
    },
  },
  methods: {
    setSort (sort: SortOption) {
      if (this.sortActive.key === sort.key) {
        if (this.sortActive.order === 'desc') {
          this.$emit('update:sortActive', { key: sort.key, order: 'asc' })
        } else {
          this.$emit('update:sortActive', { key: sort.key, order: 'desc' })
        }
      } else {
        this.$emit('update:sortActive', { key: sort.key, order: 'desc' })
      }
    },
  },
})
</script>

<style scoped>

</style>
