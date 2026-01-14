<template>
  <ItemCard
    :id="id"
    :title="title"
    :lines="lines"
    :poster-url="seriesThumbnailUrl(series.id)"
    :top-right="unreadCount"
    :top-right-icon="isRead ? 'i-mdi:check' : undefined"
    fab-icon="i-mdi:play"
    :quick-action-icon="quickActionIcon"
    :menu-icon="menuIcon"
    :menu-mouse-enter="(event: Event) => (menuActivator = event.currentTarget as Element)"
    v-bind="props"
    @mouseenter="editMetadataActivator = `#${id}`"
    @selection="(val) => emit('selection', val)"
    @click-quick-action="showEditMetadataDialog()"
    @card-long-press="bottomSheet = true"
  />
  <SeriesMenu
    :series="series"
    :activator="menuActivator"
  />
  <SeriesMenuBottomSheet
    v-model="bottomSheet"
    :series="series"
  />
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { seriesThumbnailUrl } from '@/api/images'
import { useIntl } from 'vue-intl'
import type { ItemCardEmits, ItemCardLine, ItemCardProps, ItemCardTitle } from '@/types/ItemCard'
import { useCurrentUser } from '@/colada/users'
import { useEditSeriesMetadataDialog } from '@/composables/series'

const intl = useIntl()

const id = useId()

const { series, ...props } = defineProps<
  {
    series: components['schemas']['SeriesDto']
  } & ItemCardProps
>()
const emit = defineEmits<ItemCardEmits>()

const bottomSheet = ref(false)

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
const menuIcon = computed(() => (isAdmin.value ? 'i-mdi:dots-vertical' : undefined))

const {
  prepareDialog: prepareEditSeriesMetadataDialog,
  showDialog: showEditSeriesMetadataDialog,
  activator: editMetadataActivator,
} = useEditSeriesMetadataDialog()

function showEditMetadataDialog() {
  prepareEditSeriesMetadataDialog(series)
  showEditSeriesMetadataDialog()
}

const menuActivator = ref()
</script>
