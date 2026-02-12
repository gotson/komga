<template>
  <v-list v-if="items.length > 0">
    <FilterAnyAll
      v-if="showModeSelector"
      v-model="modelMode"
      class="position-absolute top-0 right-0 mt-2 mr-2 semi-transparent"
      style="z-index: 999"
      :props="{ color: color, density: 'comfortable', rounded: 'lg' }"
    />
    <v-virtual-scroll :items="items">
      <template #default="{ item }">
        <FilterTriState
          v-model="internalModel[JSON.stringify(item)]"
          :label="item.title"
          :tri-state="!!item.valueExclude"
          :color="color"
          @change="(newVal, oldVal) => internalUpdate(item, newVal, oldVal)"
        />
      </template>
    </v-virtual-scroll>
    <div
      v-intersect.quiet="
        (isIntersecting: boolean) => (isIntersecting ? emit('loadMore') : undefined)
      "
    ></div>
  </v-list>
</template>

<script setup lang="ts">
import type { IncludeExclude } from '@/components/filter/TriState.vue'
import type { AnyAll } from '@/types/filter'

export type ItemType<T> = {
  title: string
  value: T
  valueExclude?: T
}

const model = defineModel<unknown[]>({ default: [] })
const modelMode = defineModel<AnyAll>('mode', { default: 'anyOf' })

const {
  items = [],
  color,
  showModeSelector = false,
} = defineProps<{
  items?: ItemType<unknown>[]
  color?: string
  showModeSelector?: boolean
}>()

const emit = defineEmits<{
  loadMore: []
}>()

const internalModel = ref<Record<string, IncludeExclude>>({})

function toModel(item: ItemType<unknown>, value: IncludeExclude) {
  if (value === 'include') return item.value
  if (value === 'exclude') return item.valueExclude
  return undefined
}

function internalUpdate(item: ItemType<unknown>, newVal: IncludeExclude, oldVal: IncludeExclude) {
  const oldEl = toModel(item, oldVal)
  const newEl = toModel(item, newVal)

  // remove old element if present
  const oldIndex = model.value.findIndex((it) => JSON.stringify(it) === JSON.stringify(oldEl))
  if (oldIndex >= 0) model.value.splice(oldIndex, 1)

  // add new element if defined
  if (newEl) model.value.push(newEl)
}

watchEffect(() => {
  internalModel.value = model.value.reduce((acc, item) => {
    const itemInclude = items.find((it) => JSON.stringify(it.value) === JSON.stringify(item))
    if (itemInclude)
      return {
        ...(acc as Record<number, string | undefined>),
        [JSON.stringify(itemInclude)]: 'include',
      }

    const itemExclude = items.find((it) => JSON.stringify(it.valueExclude) === JSON.stringify(item))
    if (itemExclude)
      return {
        ...(acc as Record<number, string | undefined>),
        [JSON.stringify(itemExclude)]: 'exclude',
      }

    return acc
  }, {}) as Record<string, IncludeExclude>
})
</script>

<script lang="ts"></script>

<style scoped>
.semi-transparent {
  opacity: 0.5;
}

.semi-transparent:hover {
  opacity: 1;
}
</style>
