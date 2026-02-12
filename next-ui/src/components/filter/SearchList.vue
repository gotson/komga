<template>
  <v-text-field
    v-model="search"
    clearable
    hide-details
    :loading="searchLoading"
    :label="
      $formatMessage({
        description: 'Filter search field',
        defaultMessage: 'Search',
        id: 'bzY8FH',
      })
    "
  >
  </v-text-field>

  <v-list v-if="search && searchItems.length === 0">
    <v-list-item>
      <slot name="no-data">{{
        $formatMessage({
          description: 'Search Filter: no results',
          defaultMessage: 'No results',
          id: '/NAG9i',
        })
      }}</slot>
    </v-list-item>
  </v-list>

  <FilterList
    v-model="model"
    v-model:mode="modelMode"
    :items="shownItems"
    color="primary"
    :show-mode-selector="showModeSelector"
    @load-more="emit('loadMore')"
  ></FilterList>
</template>

<script setup lang="ts">
import type { ItemType } from '@/components/filter/List.vue'
import type { AnyAll } from '@/types/filter'

const model = defineModel<unknown[]>({ default: [] })
const modelMode = defineModel<AnyAll>('mode', { default: 'anyOf' })
const search = defineModel<string>('search', { default: '' })

const {
  items = [],
  searchItems = [],
  searchLoading = false,
  showModeSelector = false,
} = defineProps<{
  items?: ItemType<unknown>[]
  searchItems?: ItemType<unknown>[]
  searchLoading?: boolean
  showModeSelector?: boolean
}>()

const emit = defineEmits<{
  loadMore: []
}>()

const shownItems = computed(() => (search.value ? searchItems : items))
</script>

<script lang="ts"></script>

<style scoped></style>
