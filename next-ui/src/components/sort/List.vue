<template>
  <v-list v-if="items.length > 0">
    <SortTriState
      v-for="(option, i) in items"
      :key="i"
      :model-value="dynamicFind(option.key).value"
      :number="multiSort ? dynamicFindIndex(option.key).value + 1 : undefined"
      :sort-option="option"
      :color="color"
      :mandatory="isChildMandatory"
      @change="(newVal, oldVal) => internalUpdate(newVal, oldVal)"
    />
  </v-list>
</template>

<script setup lang="ts">
import type { Sort } from '@/types/PageRequest'
import type { SortOption } from '@/types/sort'

const model = defineModel<Sort[]>({ default: [] })

const {
  items = [],
  color,
  multiSort = false,
  mandatory = false,
} = defineProps<{
  items?: SortOption[]
  color?: string
  /**
   * Whether multiple sorts can be applied concurrently. Defaults to false.
   */
  multiSort?: boolean
  /**
   * Whether a sort is always required. Defaults to false.
   */
  mandatory?: boolean
}>()

const dynamicFind = (key: string) => computed(() => model.value?.find((it) => it?.key === key))
const dynamicFindIndex = (key: string) =>
  computed(() => model.value?.findIndex((it) => it?.key === key))

const isChildMandatory = computed(() => {
  if (mandatory) {
    if (multiSort) return model.value.length <= 1
    else return true
  }
  return false
})

function internalUpdate(newVal: Sort | undefined, oldVal: Sort | undefined) {
  if (multiSort) {
    const oldIndex = model.value.findIndex((it) => it.key === oldVal?.key)

    // if key is not present, add to end
    if (oldIndex === -1 && newVal) model.value.push(newVal)

    // if key is present, replace or remove
    if (oldIndex >= 0) {
      if (!!newVal) {
        // replace
        model.value.splice(oldIndex, 1, newVal)
      } else {
        // remove
        model.value.splice(oldIndex, 1)
      }
    }
  } else {
    model.value = newVal ? [newVal] : []
  }
}
</script>

<script lang="ts"></script>
