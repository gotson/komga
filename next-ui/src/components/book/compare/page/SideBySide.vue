<template>
  <div class="d-flex flex-column flex-sm-row ga-4 px-2">
    <template
      v-for="item in [
        { key: 'left', data: left },
        { key: 'divider' },
        { key: 'right', data: right },
      ]"
      :key="item.key"
    >
      <!-- Centered Divider Container with Overlapping Absolute Slot -->
      <div
        v-if="item.key === 'divider'"
        class="d-flex align-center justify-center position-relative h-0 h-sm-auto w-100 w-sm-0"
      >
        <v-divider :vertical="display.smAndUp.value" />

        <!-- Absolutely positioned slot container that spills over without shifting images -->
        <div
          class="position-absolute d-flex align-center justify-center"
          style="z-index: 2; pointer-events: auto"
        >
          <slot name="center" />
        </div>
      </div>

      <v-img
        v-else
        :src="item.data?.url"
        lazy-src="@/assets/cover.svg"
        :height="display.xs.value ? 'calc(45vh - 80px)' : '75vh'"
      >
        <template #placeholder>
          <div class="d-flex align-center justify-center fill-height">
            <v-progress-circular
              color="grey"
              indeterminate
            />
          </div>
        </template>
        <div class="d-flex top-0 w-100 position-absolute justify-center align-start">
          <slot :name="`${item.key}-image-top`" />
        </div>

        <div class="d-flex bottom-0 w-100 position-absolute justify-center align-end">
          <slot :name="`${item.key}-image-bottom`" />
        </div>
      </v-img>
    </template>
  </div>
</template>

<script setup lang="ts">
import type { PageDtoWithUrl } from '@/types/BookDetails'
import { useDisplay } from 'vuetify'

const display = useDisplay()

const { left, right } = defineProps<{
  left?: PageDtoWithUrl
  right?: PageDtoWithUrl
}>()

defineSlots<{
  'left-image-top'?: (props: Record<string, never>) => unknown
  'left-image-bottom'?: (props: Record<string, never>) => unknown
  'right-image-top'?: (props: Record<string, never>) => unknown
  'right-image-bottom'?: (props: Record<string, never>) => unknown
  center?: (props: Record<string, never>) => unknown
}>()
</script>
