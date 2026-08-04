<template>
  <v-select
    v-model="model.is"
    :label="labelSelect"
    clearable
    hide-details
    :items="selectItems"
    @click:clear="model.is = undefined"
  />
  <div class="px-2 pt-2">
    <div class="d-flex justify-space-between align-center mb-2">
      <span
        class="text-body-large"
        style="min-height: 26px"
        >{{ labelRange }}</span
      >
      <v-chip
        v-if="!!model.min && !!model.max"
        :disabled="isSingle"
        closable
        color="primary"
        rounded
        size="small"
        variant="elevated"
        @click:close="clearRange()"
        >{{ model.min }} - {{ model.max }}
      </v-chip>
    </div>

    <v-range-slider
      v-model="modelRange"
      strict
      hide-details
      color="primary"
      :disabled="disabled || isSingle"
      :step="1"
      :min="min"
      :max="max"
      thumb-label="hover"
    />
  </div>
</template>

<script setup lang="ts">
import { type FilterTypeSelectRange } from '@/types/filter'

const model = defineModel<FilterTypeSelectRange>({ required: true })

const {
  labelSelect,
  labelRange,
  selectItems,
  min,
  max,
  disabled = false,
  rangeMapper = (it) => it,
} = defineProps<{
  labelSelect: string
  labelRange: string
  selectItems: unknown[]
  min?: number
  max?: number
  disabled?: boolean
  rangeMapper?: (number: number) => string | number
}>()

const isSingle = computed(() => model.value.is !== undefined)

const modelRange = computed({
  get: () => [model.value.min || 0, model.value.max || 10000],
  set: (newValue) => {
    if (newValue) {
      model.value.min = rangeMapper(newValue[0] as number)
      model.value.max = rangeMapper(newValue[1] as number)
    }
  },
})

function clearRange() {
  model.value.min = undefined
  model.value.max = undefined
}
</script>

<script lang="ts"></script>

<style scoped></style>
