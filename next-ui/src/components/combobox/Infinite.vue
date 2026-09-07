<template>
  <v-autocomplete
    v-model="model"
    v-model:search="search"
    :items="displayedItems"
    :label="label"
    chips
    closable-chips
    multiple
    hide-details="auto"
    :hide-no-data="false"
    no-auto-scroll
    @keydown.enter="acceptAndClear"
    @keydown.esc="search = undefined"
  >
    <template #prepend>
      <slot name="prepend" />
    </template>

    <!-- Infinite scroll trigger at bottom of list -->
    <template
      v-if="!isSearching && hasMoreData"
      #append-item
    >
      <div
        v-intersect.quiet="
          (isIntersecting: boolean) => (isIntersecting ? emit('loadMore') : undefined)
        "
      ></div>
    </template>

    <template
      v-if="isSearching"
      #no-data
    >
      <v-list-item @click="acceptAndClear">
        <v-list-item-title class="text-wrap">
          <FormattedMessage
            :message-descriptor="message"
            :values="{ search: search }"
          >
            <template #kbd="Content">
              <kbd>
                <component :is="Content" />
              </kbd>
            </template>
            <template #b="Content">
              <strong>
                <component :is="Content" />
              </strong>
            </template>
          </FormattedMessage>
        </v-list-item-title>
      </v-list-item>
    </template>
  </v-autocomplete>
</template>

<script setup lang="ts">
import { defineMessage } from 'vue-intl'

const model = defineModel<string[]>({ default: () => [] })
const search = defineModel<string | undefined>('search', { default: '' })

const { items = [], hasMoreData = false } = defineProps<{
  items?: string[]
  hasMoreData?: boolean
  label?: string
}>()

const emit = defineEmits<{
  loadMore: []
}>()

const isSearching = computed(() => (search.value?.trim()?.length ?? 0) > 0)

const displayedItems = computed(() => {
  return Array.from(new Set([...items, ...model.value]))
})

function acceptAndClear() {
  if (search.value && !model.value.includes(search.value)) {
    model.value.push(search.value)
  }
  search.value = undefined
}

const message = defineMessage({
  description: 'Combobox: no result matching search',
  defaultMessage:
    'No results matching "<b>{search}</b>". Press <kbd>enter</kbd> or click to create a new one.',
  id: 'iCYg8R',
})
</script>

<script lang="ts"></script>

<style scoped></style>
