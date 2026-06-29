<template>
  <ItemMenuSheet
    v-model="isShown"
    :actions="actionsDefault"
    :manage-actions="actionsManagement"
    :activator="activator"
    :sheet-title="series.metadata.title"
  />
</template>

<script setup lang="ts">
import { useSeriesActions } from '@/composables/series/useSeriesActions'
import { createOrderCompareFn } from '@/functions/sort'
import { ActionName } from '@/types/action/action'
import { useSelectionStore } from '@/stores/selection'
import { useSelectAction } from '@/composables/selection'
import { usePrimaryInput } from '@/composables/device'
import type { SeriesDto } from '@/generated/openapi'

const isShown = defineModel<boolean>({ default: false })

const {
  activator,
  series,
  excludeActions = [],
} = defineProps<{
  activator: string | Element
  series: SeriesDto
  excludeActions?: ActionName[]
}>()

const main = [
  ActionName.OPEN_READER,
  ActionName.OPEN_READER_INCOGNITO,
  ActionName.MARK_READ,
  ActionName.MARK_UNREAD,
  ActionName.ADD_TO_COLLECTION,
  ActionName.ADD_TO_READLIST,
  ActionName.DOWNLOAD,
]
const management = [
  ActionName.EDIT_SERIES,
  ActionName.REFRESH_METADATA,
  ActionName.ANALYZE,
  ActionName.DELETE,
]

function afterClick() {
  isShown.value = false
}

const selectionStore = useSelectionStore()
const { isTouchPrimary } = usePrimaryInput()
const { selectAction } = useSelectAction(() => series, afterClick)

const { actions } = useSeriesActions(() => series, afterClick)
const actionsDefault = computed(() => [
  ...(selectionStore.isEmpty && isTouchPrimary.value ? [selectAction.value] : []),
  ...actions.value
    .filter((it) => main.includes(it.action) && !excludeActions.includes(it.action))
    .toSorted(createOrderCompareFn(main, (it) => it.action.toString())),
])
const actionsManagement = computed(() =>
  actions.value
    .filter((it) => management.includes(it.action) && !excludeActions.includes(it.action))
    .toSorted(createOrderCompareFn(management, (it) => it.action.toString())),
)
</script>

<script lang="ts"></script>

<style scoped></style>
