<template>
  <v-list-subheader>{{
    $formatMessage({
      description: 'Poster filter: subheader for selected section',
      defaultMessage: 'Selected',
      id: 'S9SjlA',
    })
  }}</v-list-subheader>
  <FilterList
    v-model="model"
    :items="itemsSelected"
  />

  <v-list-subheader>{{
    $formatMessage({
      description: 'Poster filter: subheader for unselected section',
      defaultMessage: 'Unselected',
      id: 'jYOsNC',
    })
  }}</v-list-subheader>
  <FilterList
    v-model="model"
    :items="itemsUnselected"
  />
</template>

<script setup lang="ts">
import { useIntl } from 'vue-intl'
import { SchemaPoster } from '@/types/filter'
import * as v from 'valibot'
import { posterMessages, PosterValues } from '@/types/Poster'
import type { ItemType } from '@/components/filter/List.vue'

const intl = useIntl()

type SchPoster = v.InferOutput<typeof SchemaPoster>

const model = defineModel<SchPoster[]>({ default: () => [] })

const itemsSelected = buildList(true)
const itemsUnselected = buildList(false)

function buildList(selected: boolean): ItemType<SchPoster>[] {
  return PosterValues.map((it) => {
    return {
      title: intl.formatMessage(posterMessages[it]),
      value: { i: 'i', v: [it, selected] },
      valueExclude: { i: 'e', v: [it, selected] },
    }
  })
}
</script>
