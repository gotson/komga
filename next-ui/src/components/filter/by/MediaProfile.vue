<template>
  <FilterList
    v-model="model"
    :items="items"
  />
</template>

<script setup lang="ts">
import * as v from 'valibot'
import { SchemaMediaProfile } from '@/types/filter'
import { useIntl } from 'vue-intl'
import { MediaProfile, mediaProfileMessages } from '@/types/MediaProfile'

type MediaProfile = v.InferOutput<typeof SchemaMediaProfile>

const intl = useIntl()

const model = defineModel<MediaProfile[]>({ default: [] })

const items: {
  title: string
  value: MediaProfile
}[] = Object.values(MediaProfile).map((it) => ({
  title: intl.formatMessage(mediaProfileMessages[it]),
  value: it,
}))
</script>

<script lang="ts"></script>

<style scoped></style>
