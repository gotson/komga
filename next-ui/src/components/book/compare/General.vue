<template>
  <div class="d-flex flex-column flex-sm-row ga-8 align-center">
    <BookCompareInfo :book="left" />

    <v-icon
      :icon="arrowIcon"
      size="x-large"
    />

    <BookCompareInfo
      :book="right"
      :file-size-compared="fileSizeCompared"
      :pages-number-compared="pagesCompared"
    />
  </div>
</template>

<script setup lang="ts">
import type { BookDetails } from '@/types/BookDetails'
import { useDisplay } from 'vuetify'
import { useRtl } from 'vuetify/framework'

const display = useDisplay()
const { isRtl } = useRtl()

const {
  left,
  right,
  isUpgrade = false,
} = defineProps<{
  left: BookDetails
  right: BookDetails
  isUpgrade?: boolean
}>()

const arrowIcon = computed(() => {
  if (isUpgrade) {
    if (display.xs.value) return 'i-mdi:arrow-down-bold'
    if (isRtl.value) return 'i-mdi:arrow-left-bold'
    return 'i-mdi:arrow-right-bold'
  }
  if (display.xs.value) return 'i-mdi:arrow-up-down-bold'
  return 'i-mdi:arrow-left-right-bold'
})

const pagesCompared = computed(() => {
  if (isUpgrade) {
    if (right.pages.length > left.pages.length) return 'more'
    if (right.pages.length < left.pages.length) return 'less'
  }
})

const fileSizeCompared = computed(() => {
  if (isUpgrade) {
    if (right.fileSize > left.fileSize) return 'more'
    if (right.fileSize < left.fileSize) return 'less'
  }
})
</script>
