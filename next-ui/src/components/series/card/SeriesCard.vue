<template>
  <ItemCard
    :id="id"
    :title="title"
    :lines="lines"
    :poster-url="seriesPosterUrl(series.id)"
    :top-right="unreadCount"
    :top-right-icon="isRead ? 'i-mdi:check' : undefined"
    :fab-icon="canRead ? 'i-mdi:play' : undefined"
    :quick-action-icon="quickActionIcon"
    :quick-action-props="quickActionProps"
    :menu-icon="menuIcon"
    :menu-props="menuProps"
    :card-to="`/series/${series.id}`"
    v-bind="propsLeft"
    @selection="(val, event) => emit('selection', val, event)"
    @click-quick-action="showEditMetadataDialog()"
    @card-long-press="bottomSheet = true"
    @click-fab="openReader"
  />
  <SeriesMenuSheet
    v-model="bottomSheet"
    :series="series"
    :activator="menuActivator"
  />
</template>

<script setup lang="ts">
import { seriesPosterUrl } from '@/api/images'
import { useIntl } from 'vue-intl'
import type { ItemCardEmits, ItemCardLine, ItemCardProps, ItemCardTitle } from '@/types/ItemCard'
import { useCurrentUser } from '@/colada/users'

import { useEditSeriesMetadataDialog } from '@/composables/series/useEditSeriesMetadataDialog'
import { useSeriesBooks } from '@/composables/series/useSeriesBooks'
import { useSeries } from '@/composables/series/useSeries'
import type { SeriesDto } from '@/generated/openapi'

const intl = useIntl()

const id = useId()

const props = defineProps<
  {
    series: SeriesDto
  } & ItemCardProps
>()
const emit = defineEmits<ItemCardEmits>()

const bottomSheet = ref(false)

const series = toRef(props, 'series')
const propsLeft = computed(() => {
  const { series, ...rest } = props
  return rest
})

const { isRead, unreadCount, canRead } = useSeries(series)

const title = computed<ItemCardTitle>(() => ({
  text: series.value.metadata.title,
  lines: 2,
  routerLink: `/series/${series.value.id}`,
}))

const lines = computed<ItemCardLine[]>(() => {
  if (series.value.deleted)
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

  if (series.value.oneshot) {
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
          defaultMessage: `{count, plural,
one {# book}
other {# books}
}`,
          id: 'cGOJnB',
        },
        { count: series.value.booksCount },
      ),
    },
  ]
})

const { isAdmin } = useCurrentUser()
const quickActionIcon = computed(() => (isAdmin.value ? 'i-mdi:pencil' : undefined))
const quickActionProps = computed(() => ({
  id: `${id}_quick`,
  onmouseenter: () => (editMetadataActivator.value = `#${id}_quick`),
}))
const menuIcon = computed(() => (isAdmin.value ? 'i-mdi:dots-vertical' : undefined))
const menuProps = computed(() => ({
  onmouseenter: (event: Event) => (menuActivator.value = event.currentTarget as Element),
}))

const {
  prepareDialog: prepareEditSeriesMetadataDialog,
  showDialog: showEditSeriesMetadataDialog,
  activator: editMetadataActivator,
} = useEditSeriesMetadataDialog()

function showEditMetadataDialog() {
  prepareEditSeriesMetadataDialog(series.value)
  showEditSeriesMetadataDialog()
}

const menuActivator = ref()

const { readFirstBook } = useSeriesBooks(series.value.id)
function openReader() {
  void readFirstBook()
}
</script>
