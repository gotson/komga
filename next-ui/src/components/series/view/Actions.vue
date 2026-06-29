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
    :exclude-actions="exclude"
  />
</template>

<script setup lang="ts">
import { useSeriesActions } from '@/composables/series/useSeriesActions'
import { createOrderCompareFn } from '@/functions/sort'
import { commonMessages } from '@/utils/i18n/common-messages'
import { ActionName } from '@/types/action/action'
import type { SeriesDto } from '@/generated/openapi'

const props = defineProps<{
  series: SeriesDto
}>()

const id = useId()
const { actions } = useSeriesActions(() => props.series)

const bothReadUnread = computed(
  () =>
    actions.value.some((it) => it.action === ActionName.MARK_READ) &&
    actions.value.some((it) => it.action === ActionName.MARK_UNREAD),
)

const bottomSheet = ref(false)
const prominent = computed(() => [
  ActionName.OPEN_READER_INCOGNITO,
  ActionName.MARK_READ,
  ...(bothReadUnread.value ? [] : [ActionName.MARK_UNREAD]),
  ActionName.EDIT_SERIES,
])

const exclude = [...prominent.value, ActionName.OPEN_READER]

const prominentActions = computed(() =>
  actions.value
    .filter((it) => prominent.value.includes(it.action))
    .toSorted(createOrderCompareFn(prominent.value, (it) => it.action.toString())),
)
const readAction = computed(() => actions.value.find((it) => it.action === ActionName.OPEN_READER))
const hasExtra = computed(() => actions.value.some((it) => !exclude.includes(it.action)))
</script>
