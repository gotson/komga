<template>
  <ItemMenuBottomSheet
    v-model="isShown"
    :actions="actionsDefault"
    :manage-actions="actionsManagement"
  />
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { useSeriesActions } from '@/composables/series/useSeriesActions'
import { SeriesAction, seriesActionGroups } from '@/types/series'
import { createOrderCompareFn } from '@/functions/sort'

const isShown = defineModel<boolean>({ default: false })

const { series, excludeActions = [] } = defineProps<{
  series: components['schemas']['SeriesDto']
  excludeActions?: SeriesAction[]
}>()

function afterClick() {
  isShown.value = false
}
const { actions } = useSeriesActions(() => series, afterClick)
const actionsDefault = computed(() =>
  actions.value
    .filter(
      (it) => seriesActionGroups.default.includes(it.action) && !excludeActions.includes(it.action),
    )
    .toSorted(createOrderCompareFn(seriesActionGroups.default, (it) => it.action.toString())),
)
const actionsManagement = computed(() =>
  actions.value
    .filter(
      (it) =>
        seriesActionGroups.management.includes(it.action) && !excludeActions.includes(it.action),
    )
    .toSorted(createOrderCompareFn(seriesActionGroups.management, (it) => it.action.toString())),
)
</script>

<script lang="ts"></script>

<style scoped></style>
