<template>
  <ItemCardWide
    :title="series.metadata.title"
    :text="series.metadata.summary"
    :poster-url="seriesPosterUrl(series.id, cacheStore.getVersion(series.id))"
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
  <SeriesMenuSheet
    v-model="bottomSheet"
    :series="series"
    :activator="menuActivator"
  />
</template>

<script setup lang="ts">
import { seriesPosterUrl } from '@/api/images'
import type { ItemCardEmits, ItemCardProps } from '@/types/ItemCard'
import { useCurrentUser } from '@/colada/users'

import { useEditSeriesMetadataDialog } from '@/composables/series/useEditSeriesMetadataDialog'
import type { SeriesDto } from '@/generated/openapi'
import { useImageCacheStore } from '@/stores/image-cache'

const cacheStore = useImageCacheStore()

const { series, ...props } = defineProps<
  {
    series: SeriesDto
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
