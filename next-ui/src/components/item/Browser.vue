<template>
  <v-data-iterator
    v-model="selectionStore.selection"
    return-object
    :items="items"
    :items-per-page="appStore.browsingPaging === 'paged' ? itemsPerPage : -1"
    :page="appStore.browsingPaging === 'paged' ? page1 : 1"
    show-select
  >
    <template #default="{ items: internalItems, toggleSelect, isSelected }">
      <v-container fluid>
        <v-row>
          <v-col
            v-for="(item, idx) in internalItems"
            :key="idx"
            :cols="presentationMode === 'grid' ? 'auto' : 12"
          >
            <slot
              :item="item.raw"
              :is-selected="isSelected(item)"
              :pre-select="selectionStore.isNotEmpty"
              :toggle-select="(event: MouseEvent) => toggleSelect(item, idx, event)"
            />
          </v-col>
        </v-row>
      </v-container>
    </template>
  </v-data-iterator>

  <v-pagination
    v-if="appStore.isBrowsingPaged"
    v-model="page1"
    :length="pageCount"
  />

  <div
    v-if="appStore.isBrowsingScroll && hasNextPage"
    v-intersect="(isIntersecting: boolean) => (isIntersecting ? emit('loadNextPage') : undefined)"
    style="min-height: 40px"
  ></div>
</template>

<script setup lang="ts" generic="T">
import { useAppStore } from '@/stores/app'
import { useSelectionStore } from '@/stores/selection'
import type { PresentationMode } from '@/types/libraries'
import { useItemsPerPage } from '@/composables/pagination'

const appStore = useAppStore()

const page1 = defineModel<number>('page1', { required: true })

const { items, presentationMode, hasNextPage, pageCount } = defineProps<{
  items?: T[]
  presentationMode: PresentationMode
  hasNextPage: boolean
  pageCount: number
}>()

const emit = defineEmits<{
  loadNextPage: []
}>()

const selectionStore = useSelectionStore()
const { itemsPerPage } = useItemsPerPage(appStore.browsingPageSize)
</script>
