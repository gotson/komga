<template>
  <FilterList
    v-model="model"
    :items="items"
  />
</template>

<script setup lang="ts">
import { useIntl } from 'vue-intl'
import { MediaProfileValues, type MediaProfile, mediaProfileMessages } from '@/types/MediaProfile'

const intl = useIntl()

const model = defineModel<MediaProfile[]>({ default: [] })

const items: {
  title: string
  value: MediaProfile
}[] = MediaProfileValues.map((it) => ({
  title: intl.formatMessage(mediaProfileMessages[it]),
  value: it,
}))
</script>

<script lang="ts"></script>

<style scoped></style>
