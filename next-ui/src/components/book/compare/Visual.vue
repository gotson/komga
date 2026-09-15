<template>
  <BookComparePageSideBySide
    :left="left[pageNumberLeft - 1]"
    :right="right[pageNumberRight - 1]"
  >
    <template #left-image-top>
      <BookComparePageStepper
        v-model="pageNumberLeft"
        :max="left.length"
        :control-props="controlProps"
        pill
        hide-when-idle
        class="mt-2"
      />
    </template>

    <template #[targetRightSlot]>
      <BookComparePageStepper
        v-model="pageNumberRight"
        :max="right.length"
        :control-props="controlProps"
        pill
        hide-when-idle
        class="mt-2"
      />
    </template>

    <template #center>
      <BookComparePageStepper
        hide-count
        hide-when-idle
        :control-props="controlProps"
        :disable-prev="centerDisablePrev"
        :disable-next="centerDisableNext"
        pill
        @click:first="bothFirst()"
        @click:previous="bothPrevious()"
        @click:next="bothNext()"
        @click:last="bothLast()"
      />
    </template>
  </BookComparePageSideBySide>
</template>

<script setup lang="ts">
import type { PageDtoWithUrl } from '@/types/BookDetails'
import { watchImmediate } from '@vueuse/core'
import { clamp } from '@/functions/clamp'
import { useDisplay } from 'vuetify'
import { getSurroundingElements } from '@/functions/array'
import { useImagePrefetch } from '@/composables/image'

const display = useDisplay()

const { left, right } = defineProps<{
  left: PageDtoWithUrl[]
  right: PageDtoWithUrl[]
}>()

const pageNumberLeft = defineModel<number>('pageNumberLeft', { default: 1 })
const pageNumberRight = defineModel<number>('pageNumberRight', { default: 1 })

watchImmediate(pageNumberLeft, (val) => {
  const clamped = clamp(val, 1, left.length)
  if (clamped !== val) pageNumberLeft.value = clamped
})
watchImmediate(pageNumberRight, (val) => {
  const clamped = clamp(val, 1, right.length)
  if (clamped !== val) pageNumberRight.value = clamped
})

const controlProps = {
  color: 'surface',
  variant: 'flat',
}

const targetRightSlot = computed(() =>
  display.xs.value ? 'right-image-bottom' : 'right-image-top',
)
const centerDisablePrev = computed(() => pageNumberLeft.value <= 1 && pageNumberRight.value <= 1)
const centerDisableNext = computed(
  () => pageNumberLeft.value >= left.length && pageNumberRight.value >= right.length,
)

function bothFirst() {
  pageNumberLeft.value = 1
  pageNumberRight.value = 1
}
function bothPrevious() {
  pageNumberLeft.value--
  pageNumberRight.value--
}
function bothNext() {
  pageNumberLeft.value++
  pageNumberRight.value++
}
function bothLast() {
  pageNumberLeft.value = left.length
  pageNumberRight.value = right.length
}

const prefetchUrls = computed(() => [
  ...getSurroundingElements(left, pageNumberLeft.value - 1, 2, 2).map((it) => it.url!),
  ...getSurroundingElements(right, pageNumberRight.value - 1, 2, 2).map((it) => it.url!),
])
useImagePrefetch(prefetchUrls)
</script>
