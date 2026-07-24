<template>
  <div>
    <div class="d-flex align-center justify-space-between">
      <div
        v-if="title"
        class="text-title-large"
      >
        {{ title }}
      </div>

      <div
        v-if="showArrows"
        class="d-flex align-center"
      >
        <v-icon-btn
          icon="i-mdi:chevron-left"
          :disabled="!slideGroup?.hasPrev"
          variant="text"
          @click="slideGroup?.scrollTo('prev')"
        />
        <v-icon-btn
          icon="i-mdi:chevron-right"
          :disabled="!slideGroup?.hasNext"
          variant="text"
          @click="slideGroup?.scrollTo('next')"
        />
      </div>
    </div>

    <v-slide-group
      ref="slideGroup"
      multiple
      show-arrows="never"
      :scroll-to-active="false"
    >
      <v-slide-group-item
        v-for="item in items"
        :key="item.id"
        :value="item"
      >
        <slot
          :item="item"
          :is-selected="selectionStore.isSelected(item)"
          :pre-select="selectionStore.isNotEmpty"
          :toggle-select="() => selectionStore.toggle(item)"
        />
      </v-slide-group-item>

      <div
        v-if="hasNextPage"
        v-intersect="
          (isIntersecting: boolean) => (isIntersecting ? emit('loadNextPage') : undefined)
        "
        style="min-width: 40px"
      />
    </v-slide-group>
  </div>
</template>

<script setup lang="ts">
import { VSlideGroup } from 'vuetify/components'
import { usePrimaryInput } from '@/composables/device'
import { type SelectionType, useSelectionStore } from '@/stores/selection'

const { isTouchPrimary } = usePrimaryInput()

const slideGroup = ref<InstanceType<typeof VSlideGroup> | null>(null)
const showArrows = computed(
  () => !isTouchPrimary.value && (slideGroup.value?.hasPrev || slideGroup.value?.hasNext),
)

defineProps<{
  items?: SelectionType[]
  title?: string
  hasNextPage: boolean
}>()

const emit = defineEmits<{
  loadNextPage: []
}>()

const selectionStore = useSelectionStore()
</script>

<style scoped></style>
