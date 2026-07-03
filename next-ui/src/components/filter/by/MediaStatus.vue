<template>
  <FilterList
    v-model="model"
    :items="items"
  />
</template>

<script setup lang="ts">
import { useIntl } from 'vue-intl'
import { MediaStatusValues, type MediaStatus, mediaStatusMessages } from '@/types/MediaStatus'

const intl = useIntl()

const model = defineModel<MediaStatus[]>({ default: [] })

const items: {
  title: string
  value: MediaStatus
}[] = MediaStatusValues.map((it) => ({
  title: intl.formatMessage(mediaStatusMessages[it]),
  value: it,
}))
</script>

<script lang="ts"></script>

<style scoped></style>
