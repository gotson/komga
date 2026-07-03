<template>
  <FilterList
    v-model="model"
    :items="items"
  />
</template>

<script setup lang="ts">
import { SeriesStatusValues, type SeriesStatus, seriesStatusMessages } from '@/types/SeriesStatus'
import { useIntl } from 'vue-intl'

const intl = useIntl()

const model = defineModel<SeriesStatus[]>({ default: [] })

const items: {
  title: string
  value: SeriesStatus
}[] = SeriesStatusValues.map((it) => ({
  title: intl.formatMessage(seriesStatusMessages[it]),
  value: it,
}))
</script>

<script lang="ts"></script>

<style scoped></style>
