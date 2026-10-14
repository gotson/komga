<template>
  <v-list :lines="lines">
    <ReorderHistoryScroller
      ref="listRef"
      v-model="model"
    >
      <template #item="{ item: id, deleteItem, handleClass }">
        <slot
          :id="id"
          name="item"
          :delete-item="deleteItem"
          :handle-class="handleClass"
        />
      </template>
    </ReorderHistoryScroller>
  </v-list>
</template>

<script setup lang="ts">
import type ReorderHistoryScroller from '@/components/ReorderHistoryScroller.vue'

// list of IDs
const model = defineModel<string[]>({ required: true })

const { lines = 'two' } = defineProps<{
  lines?: 'one' | 'two' | 'three'
}>()

const listRef = useTemplateRef<InstanceType<typeof ReorderHistoryScroller>>('listRef')

defineExpose({
  canUndo: computed(() => (listRef && listRef.value?.canUndo) || false),
  canRedo: computed(() => (listRef && listRef.value?.canRedo) || false),
  undo: () => listRef?.value?.undo(),
  redo: () => listRef?.value?.redo(),
})
</script>
