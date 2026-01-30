<template>
  <ItemCardWide
    :title="series.metadata.title"
    :text="series.metadata.summary"
    :poster-url="seriesThumbnailUrl(series.id)"
    :top-right="unreadCount"
    :top-right-icon="isRead ? 'i-mdi:check' : undefined"
    :quick-action-icon="quickActionIcon"
    :quick-action-props="quickActionProps"
    :menu-icon="menuIcon"
    :menu-props="menuProps"
    v-bind="props"
    @selection="(val, event) => emit('selection', val, event)"
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
import type { ItemCardEmits, ItemCardProps } from '@/types/ItemCard'
import { useCurrentUser } from '@/colada/users'

import { useEditSeriesMetadataDialog } from '@/composables/series/useEditSeriesMetadataDialog'

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

const { isAdmin } = useCurrentUser()
const quickActionIcon = computed(() => (isAdmin.value ? 'i-mdi:pencil' : undefined))
const quickActionProps = computed(() => ({
  onMouseenter: (event: Event) => (editMetadataActivator.value = event.currentTarget as Element),
}))
const menuIcon = computed(() => (isAdmin.value ? 'i-mdi:dots-vertical' : undefined))
const menuProps = computed(() => ({
  onmouseenter: (event: Event) => (menuActivator.value = event.currentTarget as Element),
}))

const { prepareDialog: showEditSeriesMetadataDialog, activator: editMetadataActivator } =
  useEditSeriesMetadataDialog()

function showEditMetadataDialog() {
  showEditSeriesMetadataDialog(series)
}

const menuActivator = ref()
</script>
