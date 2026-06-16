<template>
  <div class="d-flex ga-2">
    <v-btn
      v-if="readAction"
      :prepend-icon="readAction.icon"
      :text="readAction.title"
      :disabled="readAction.disabled"
      @click="readAction.onClick?.()"
    />

    <v-icon-btn
      v-for="action in prominentActions"
      :key="action.action"
      v-tooltip:bottom="action.title"
      v-bind="action"
    />

    <v-icon-btn
      v-if="hasExtra"
      :id="id"
      v-tooltip:bottom="$formatMessage(commonMessages.buttonMore)"
      icon="i-mdi:dots-horizontal"
      @click="bottomSheet = true"
    />
  </div>
  <SeriesMenuSheet
    v-model="bottomSheet"
    :series="series"
    :activator="`#${id}`"
    :exclude-actions="excludeActions"
  />
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { useSeriesActions } from '@/composables/series/useSeriesActions'
import { SeriesAction, seriesActionGroups } from '@/types/series'
import { createOrderCompareFn } from '@/functions/sort'
import { commonMessages } from '@/utils/i18n/common-messages'

const props = defineProps<{
  series: components['schemas']['SeriesDto']
}>()

const id = useId()
const { actions } = useSeriesActions(() => props.series)

const bottomSheet = ref(false)

const prominentActions = computed(() =>
  actions.value
    .filter((it) => seriesActionGroups.seriesView.includes(it.action))
    .toSorted(createOrderCompareFn(seriesActionGroups.seriesView, (it) => it.action.toString())),
)
const readAction = computed(() =>
  actions.value.find((it) => it.action === SeriesAction.OPEN_READER),
)
const excludeActions = [...seriesActionGroups.seriesView, SeriesAction.OPEN_READER]
const hasExtra = computed(() => actions.value.some((it) => !excludeActions.includes(it.action)))
</script>
