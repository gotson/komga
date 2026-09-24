<template>
  <div v-if="shouldShow">
    <v-btn
      v-ktooltip:bottom="messagePrev"
      :icon="isRtl ? 'i-mdi:chevron-right' : 'i-mdi:chevron-left'"
      :disabled="!previous"
      :to="{
        name: '/book/[id]',
        params: { id: previous?.id || 'none' },
        query: formatBrowsingContextAsQueryParam(toValue(context)),
      }"
    />
    <v-btn
      v-ktooltip:bottom="messageNext"
      :icon="isRtl ? 'i-mdi:chevron-left' : 'i-mdi:chevron-right'"
      :disabled="!next"
      :to="{
        name: '/book/[id]',
        params: { id: next?.id || 'none' },
        query: formatBrowsingContextAsQueryParam(toValue(context)),
      }"
    />
  </div>
</template>

<script setup lang="ts">
import { useRtl } from 'vuetify/framework'
import { useBookNavigation } from '@/composables/book/useBookNavigation'
import { formatBrowsingContextAsQueryParam } from '@/functions/browsing-context'
import { useIntl } from 'vue-intl'

const props = defineProps<{
  bookId: string
}>()

const intl = useIntl()
const { isRtl } = useRtl()
const { previous, next, context, top } = useBookNavigation(() => props.bookId)
const shouldShow = computed(() => top.value?.type === 'series' || top.value?.type === 'readList')

const messagePrev = computed(() =>
  top.value?.type === 'readList'
    ? intl.formatMessage({
        description: 'Book navigation within read list: previous button tootlip',
        defaultMessage: 'Go to previous book within read list',
        id: 'Xq4kkn',
      })
    : intl.formatMessage({
        description: 'Book navigation within series: previous button tootlip',
        defaultMessage: 'Go to previous book within series',
        id: 'P9B8fE',
      }),
)
const messageNext = computed(() =>
  top.value?.type === 'readList'
    ? intl.formatMessage({
        description: 'Book navigation within read list: next button tootlip',
        defaultMessage: 'Go to next book within read list',
        id: 'Rw2Iwj',
      })
    : intl.formatMessage({
        description: 'Book navigation within series: next button tootlip',
        defaultMessage: 'Go to next book within series',
        id: 'I75Xgh',
      }),
)
</script>
