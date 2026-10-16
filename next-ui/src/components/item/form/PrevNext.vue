<template>
  <div>
    <v-btn
      :text="
        $formatMessage({
          description: 'Edit item dialog: previous item button',
          defaultMessage: 'Previous',
          id: 'pK72Vk',
        })
      "
      :disabled="!prev"
      @click="emit('previous', prev!)"
    />
    <v-btn
      :text="
        $formatMessage({
          description: 'Edit item dialog: next item button',
          defaultMessage: 'Next',
          id: 'iGdw+C',
        })
      "
      :disabled="!next"
      @click="emit('next', next!)"
    />
  </div>
</template>

<script setup lang="ts" generic="T extends { id: string }">
import { findAdjacent } from '@/functions/array'

export type PrevNextProps<T> = {
  items: T[]
  current: T
}

const props = defineProps<PrevNextProps<T>>()

const emit = defineEmits<{
  previous: [item: T]
  next: [item: T]
}>()

const prev = computed(() => findAdjacent(props.items, (it) => it.id === props.current.id, 'before'))
const next = computed(() => findAdjacent(props.items, (it) => it.id === props.current.id, 'after'))
</script>
