<template>
  <ItemCard
    :title="title"
    :lines="lines"
    :poster-url="seriesThumbnailUrl(series.id)"
    :top-right="unreadCount"
    :top-right-icon="isRead ? 'i-mdi:check' : undefined"
    fab-icon="i-mdi:play"
    :quick-action-icon="quickActionIcon"
    v-bind="props"
    @selection="(val) => emit('selection', val)"
  />
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { seriesThumbnailUrl } from '@/api/images'
import { useIntl } from 'vue-intl'
import type { ItemCardEmits, ItemCardLine, ItemCardProps, ItemCardTitle } from '@/types/ItemCard'
import { useCurrentUser } from '@/colada/users'

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

const title = computed<ItemCardTitle>(() => ({ text: series.metadata.title, lines: 2 }))

const lines = computed<ItemCardLine[]>(() => {
  if (series.deleted)
    return [
      {
        text: intl.formatMessage({
          description: 'Series card subtitle: unavailable',
          defaultMessage: 'Unavailable',
          id: 'wbH42A',
        }),
        classes: 'text-error',
      },
    ]

  if (series.oneshot) {
    return [
      {
        text: intl.formatMessage({
          description: 'Series card subtitle: oneshot',
          defaultMessage: 'One-shot',
          id: 'NKVL81',
        }),
      },
    ]
  }

  return [
    {
      text: intl.formatMessage(
        {
          description: 'Series card subtitle: count of books',
          defaultMessage: '{count} books',
          id: 'bJsa/f',
        },
        { count: series.booksCount },
      ),
    },
  ]
})

const { isAdmin } = useCurrentUser()
const quickActionIcon = computed(() => (isAdmin.value ? 'i-mdi:pencil' : undefined))
</script>
