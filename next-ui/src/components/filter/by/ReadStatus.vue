<template>
  <FilterList
    v-model="model"
    :items="items"
  />
</template>

<script setup lang="ts">
import { useIntl } from 'vue-intl'
import { ReadStatusValues, readStatusMessages } from '@/types/ReadStatus'
import type { SchemaReadStatus } from '@/types/filter'
import * as v from 'valibot'

type SchReadStatus = v.InferOutput<typeof SchemaReadStatus>

const intl = useIntl()

const model = defineModel<SchReadStatus[]>({ default: [] })

const items = ReadStatusValues.map((it) => ({
  title: intl.formatMessage(readStatusMessages[it]),
  value: { i: 'i', v: it },
  valueExclude: { i: 'e', v: it },
}))
</script>

<script lang="ts"></script>

<style scoped></style>
