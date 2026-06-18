<template>
  <v-data-iterator
    v-model="selectionStore.selection"
    return-object
    :items="items"
    :items-per-page="-1"
    :page="1"
    show-select
  >
    <template #default="{ items: internalItems, toggleSelect, isSelected }">
      <v-container fluid>
        <v-row density="comfortable">
          <v-col
            v-for="(item, idx) in internalItems"
            :key="idx"
            :cols="presentationMode === 'grid' ? (display.xs.value ? 4 : 'auto') : 12"
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

  <v-container v-if="appStore.isBrowsingPaged && pageCount > 1">
    <v-row class="align-center justify-center">
      <v-col
        cols="12"
        sm="9"
      >
        <v-pagination
          v-model="page1"
          :length="pageCount"
        />
      </v-col>
      <v-col
        cols="12"
        sm="3"
      >
        <AutocompleteMandatory
          :label="
            $formatMessage({
              description: 'Pagination jump: label',
              defaultMessage: 'Jump to',
              id: '+opDqu',
            })
          "
          placeholder="..."
          persistent-placeholder
          :items="jumpOptions"
          class="mx-auto mx-sm-0"
          @selected="page1 = $event"
        />
      </v-col>
    </v-row>
  </v-container>

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
import { useDisplay } from 'vuetify'

const appStore = useAppStore()
const display = useDisplay()

const page1 = defineModel<number>('page1', { required: true })

const props = defineProps<{
  items?: T[]
  presentationMode: PresentationMode
  hasNextPage: boolean
  pageCount: number
}>()

const emit = defineEmits<{
  loadNextPage: []
}>()

const selectionStore = useSelectionStore()

const jumpOptions = computed(() => Array.from({ length: props.pageCount }).map((_, i) => i + 1))
</script>
