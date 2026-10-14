<template>
  <VueDraggable
    v-model="model"
    :animation="150"
    ghost-class="ghost"
    :target="draggableTarget"
    :handle="'.' + handleClass"
    @start="drag = true"
    @end="onDragEnd()"
  >
    <!-- Over Threshold: Virtual Scroll -->
    <v-virtual-scroll
      v-if="isOverThreshold"
      :items="model"
    >
      <template #default="{ item, index }">
        <slot
          name="item"
          :item="item"
          :index="index"
          :handle-class="handleClass"
          :delete-item="() => deleteItem(item)"
        />
      </template>
    </v-virtual-scroll>

    <!-- Under Threshold: Fade Transition Group with v-for -->
    <div
      v-else
      class="sort-target"
    >
      <v-fade-transition
        group
        :disabled="drag"
      >
        <div
          v-for="(item, index) in model"
          :key="item"
        >
          <slot
            name="item"
            :item="item"
            :index="index"
            :handle-class="handleClass"
            :delete-item="() => deleteItem(item)"
          />
        </div>
      </v-fade-transition>
    </div>
  </VueDraggable>
</template>

<script setup lang="ts">
import { VueDraggable } from 'vue-draggable-plus'
import { useManualRefHistory } from '@vueuse/core'

const model = defineModel<string[]>({ required: true })

const { threshold = 50 } = defineProps<{
  threshold?: number
}>()

const isOverThreshold = computed(() => model.value.length > threshold)
const draggableTarget = computed(() =>
  isOverThreshold.value ? '.v-virtual-scroll__container' : '.sort-target',
)

const drag = ref(false)
const handleClass = 'handle'

const { undo, canUndo, redo, canRedo, commit } = useManualRefHistory(model, {
  capacity: 100,
  clone: true,
})

// expose undo/redo to the parent
defineExpose({
  undo,
  canUndo,
  redo,
  canRedo,
})

// element moved, commit history
function onDragEnd() {
  void nextTick(() => {
    drag.value = false
    commit()
  })
}

// element deleted, commit history
function deleteItem(id: string) {
  const index = model.value.indexOf(id)
  if (index === -1) return

  model.value = model.value.toSpliced(index, 1)

  // without this the history doesn't work properly
  void nextTick(() => {
    commit()
  })
}
</script>

<style lang="scss">
.ghost {
  opacity: 0.5;
  background: #c8ebfb;
}
</style>
