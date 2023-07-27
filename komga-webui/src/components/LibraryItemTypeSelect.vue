<template>
  <v-menu offset-y>
    <template v-slot:activator="{on}">
      <v-btn icon v-on="on">
        <v-icon>mdi-book-outline</v-icon>
      </v-btn>
    </template>
    <v-list>
      <v-list-item-group v-model="selection">

        <v-list-item v-for="(item, index) in items"
                     :key="index"
                     @click="setLibraryType(item)"
        >
          <v-list-item-title>{{ $t(`enums.item_types.${itemTypeToString(item)}`) }}</v-list-item-title>
        </v-list-item>
      </v-list-item-group>
    </v-list>
  </v-menu>
</template>

<script lang="ts">
import { ItemTypes } from '@/types/items'
import Vue from 'vue'

export default Vue.extend({
  name: 'LibraryItemTypeSelect',
  data: () => {
    return {
      selection: 0,
    }
  },
  props: {
    items: {
      type: Array,
      default: () => [ItemTypes.SERIES, ItemTypes.BOOK],
    },
    value: {
      type: Number,
      required: true,
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
    setLibraryType (type: ItemTypes) {
      this.$emit('input', type)
    },
    itemTypeToString(type: ItemTypes): string {
      return ItemTypes[type]
    },
  },
})
</script>

<style scoped>

</style>