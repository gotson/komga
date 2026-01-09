<template>
  <ItemCard
    :title="series.metadata.title"
    :line1="line1"
    :line1-classes="line1Classes"
    :poster-url="seriesThumbnailUrl(series.id)"
    :top-right="unreadCount"
    :top-right-icon="isRead ? 'i-mdi:check' : undefined"
    v-bind="props"
    @selection="(val) => emit('selection', val)"
  />
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { seriesThumbnailUrl } from '@/api/images'
import { useIntl } from 'vue-intl'
import type { ItemCardEmits, ItemCardProps } from '@/types/ItemCard'

const intl = useIntl()

const { series, ...props } = defineProps<
  {
    series: components['schemas']['SeriesDto']
  } & ItemCardProps
>()
const emit = defineEmits<ItemCardEmits>()

const isRead = computed(() => series.booksCount === series.booksReadCount)
const unreadCount = computed(() =>
  series.oneshot ? undefined : series.booksUnreadCount + series.booksInProgressCount,
)
const line1 = computed(() => {
  if (series.deleted)
    return intl.formatMessage({
      description: 'Series card subtitle: unavailable',
      defaultMessage: 'Unavailable',
      id: 'wbH42A',
    })

  if (series.oneshot) {
    return intl.formatMessage({
      description: 'Series card subtitle: oneshot',
      defaultMessage: 'One-shot',
      id: 'NKVL81',
    })
  }

  return intl.formatMessage(
    {
      description: 'Series card subtitle: count of books',
      defaultMessage: '{count} books',
      id: 'bJsa/f',
    },
    { count: series.booksCount },
  )
})
const line1Classes = computed(() => {
  if (series.deleted) return 'text-error'
  return undefined
})
</script>
