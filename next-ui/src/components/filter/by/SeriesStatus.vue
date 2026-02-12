<template>
  <FilterList
    v-model="model"
    :items="items"
    color="primary"
  />
</template>

<script setup lang="ts">
import * as v from 'valibot'
import { SchemaSeriesStatus } from '@/types/filter'
import { SeriesStatus, seriesStatusMessages } from '@/types/SeriesStatus'
import { useIntl } from 'vue-intl'

type SeriesStatus = v.InferOutput<typeof SchemaSeriesStatus>

const intl = useIntl()

const model = defineModel<SeriesStatus[]>({ default: [] })

const items: {
  title: string
  value: SeriesStatus
}[] = Object.values(SeriesStatus).map((it) => ({
  title: intl.formatMessage(seriesStatusMessages[it]),
  value: it,
}))
</script>

<script lang="ts"></script>

<style scoped></style>
