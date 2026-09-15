<template>
  <v-img
    :src="currentPage?.url"
    lazy-src="@/assets/cover.svg"
    height="75vh"
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
      <BookComparePageStepper
        v-model="pageNumber"
        :max="pages.length"
        :control-props="controlProps"
        pill
        hide-when-idle
        class="mt-2"
      />
    </div>
  </v-img>
</template>

<script setup lang="ts">
import type { PageDtoWithUrl } from '@/types/BookDetails'
import { watchImmediate } from '@vueuse/core'
import { clamp } from '@/functions/clamp'
import { getSurroundingElements } from '@/functions/array'
import { useImagePrefetch } from '@/composables/image'

const { pages } = defineProps<{
  pages: PageDtoWithUrl[]
}>()

const pageNumber = defineModel<number>('pageNumber', { default: 1 })

watchImmediate(pageNumber, (val) => {
  const clamped = clamp(val, 1, pages.length)
  if (clamped !== val) pageNumber.value = clamped
})

const currentPage = computed(() => pages[pageNumber.value - 1])

const controlProps = {
  color: 'surface',
  variant: 'flat',
}

const prefetchUrls = computed(() =>
  getSurroundingElements(pages, pageNumber.value - 1, 2, 2).map((it) => it.url!),
)
useImagePrefetch(prefetchUrls)
</script>
