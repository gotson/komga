<template>
  <v-list-item
    :title="sortOption.label"
    @click="cycle()"
  >
    <template #prepend>
      <v-icon
        :icon="icon"
        :color="!!model ? color : undefined"
      />
      <!-- Use opacity instead of model-value so that spacing is the same even when hidden -->
      <v-badge
        :content="number"
        inline
        :color="color"
        :class="!!model && !!number ? '' : 'opacity-0'"
      />
    </template>
  </v-list-item>
</template>

<script setup lang="ts">
import type { SortOption, SortOrder } from '@/types/sort'
import type { Sort } from '@/types/PageRequest'

const model = defineModel<Sort | undefined>()

const icon = computed(() => {
  if (model.value?.order === 'asc') return 'i-mdi:chevron-up'
  if (model.value?.order === 'desc') return 'i-mdi:chevron-down'
  return undefined
})

const emit = defineEmits<{
  change: [newValue: Sort | undefined, oldValue: Sort | undefined]
}>()

const {
  sortOption,
  number,
  color,
  mandatory = false,
} = defineProps<{
  sortOption: SortOption
  /**
   * Sort number, in case of multiSort
   */
  number?: number
  /**
   * Base color. Applies to the icon when the value is not `undefined`.
   */
  color?: string
  mandatory?: boolean
}>()

const states = computed(
  () =>
    [
      sortOption.initialOrder,
      ...(sortOption.invertible ? (sortOption.initialOrder === 'asc' ? ['desc'] : ['asc']) : []),
      ...(mandatory ? [] : [undefined]),
    ] as SortOrder[],
)

function cycle() {
  const index = states.value.findIndex((x) => x === model.value?.order)
  const newIndex = (index + 1) % states.value.length
  if (index === newIndex) return

  const newOrder = states.value[newIndex]
  const oldVal = model.value
  const newVal = !!newOrder ? { key: sortOption.key, order: newOrder } : undefined
  model.value = newVal
  emit('change', newVal, oldVal)
}
</script>

<script lang="ts"></script>

<style scoped></style>
