<template>
  <v-menu :activator="activator">
    <v-list density="compact">
      <v-list-item
        v-for="(action, i) in actionsDefault"
        :key="i"
        v-bind="action"
      />

      <v-list-item
        v-if="actionsManagement.length > 0"
        :title="
          $formatMessage({
            description: 'Series menu: manage',
            defaultMessage: 'Manage series',
            id: 'Ougw+k',
          })
        "
        append-icon="i-mdi:menu-right"
      >
        <v-menu
          activator="parent"
          open-on-click
          open-on-hover
          location="end"
          submenu
        >
          <v-list density="compact">
            <v-list-item
              v-for="(action, i) in actionsManagement"
              :key="i"
              v-bind="action"
            />
          </v-list>
        </v-menu>
      </v-list-item>
    </v-list>
  </v-menu>
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { useSeriesActions } from '@/composables/series/useSeriesActions'
import { SeriesAction, seriesActionGroups } from '@/types/series'
import { createOrderCompareFn } from '@/functions/sort'

const {
  activator,
  series,
  excludeActions = [],
} = defineProps<{
  activator: string | Element
  series: components['schemas']['SeriesDto']
  excludeActions?: SeriesAction[]
}>()

const { actions } = useSeriesActions(() => series)
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
