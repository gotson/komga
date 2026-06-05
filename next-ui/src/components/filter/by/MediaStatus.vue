<template>
  <FilterList
    v-model="model"
    :items="items"
  />
</template>

<script setup lang="ts">
import * as v from 'valibot'
import { SchemaMediaStatus } from '@/types/filter'
import { useIntl } from 'vue-intl'
import { MediaStatus, mediaStatusMessages } from '@/types/MediaStatus'

type MediaStatus = v.InferOutput<typeof SchemaMediaStatus>

const intl = useIntl()

const model = defineModel<MediaStatus[]>({ default: [] })

const items: {
  title: string
  value: MediaStatus
}[] = Object.values(MediaStatus).map((it) => ({
  title: intl.formatMessage(mediaStatusMessages[it]),
  value: it,
}))
</script>

<script lang="ts"></script>

<style scoped></style>
