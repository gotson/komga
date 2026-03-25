<template>
  <FilterList
    v-model="model"
    :items="items"
  />
</template>

<script setup lang="ts">
import * as v from 'valibot'
import { SchemaReadStatus } from '@/types/filter'
import { useIntl } from 'vue-intl'
import { ReadStatus, readStatusMessages } from '@/types/ReadStatus'

type SchReadStatus = v.InferOutput<typeof SchemaReadStatus>

const intl = useIntl()

const model = defineModel<SchReadStatus[]>({ default: [] })

const items = Object.values(ReadStatus).map((it) => ({
  title: intl.formatMessage(readStatusMessages[it]),
  value: { i: 'i', v: it },
  valueExclude: { i: 'e', v: it },
}))
</script>

<script lang="ts"></script>

<style scoped></style>
