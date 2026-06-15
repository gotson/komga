<template>
  <v-bottom-sheet
    v-model="isShown"
    inset
  >
    <v-list>
      <v-list-item
        v-for="(action, i) in actionsDefault"
        :key="i"
        v-bind="action"
      />

      <v-divider v-if="actionsManagement.length > 0" />

      <v-list-item
        v-for="(action, i) in actionsManagement"
        :key="i"
        v-bind="action"
      />
    </v-list>
  </v-bottom-sheet>
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
