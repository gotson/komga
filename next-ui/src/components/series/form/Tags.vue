<template>
  <v-container fluid>
    <v-row>
      <v-col>
        <ComboboxInfiniteGenre v-model="model.genres">
          <template #prepend>
            <LockIcon v-model="model.genresLock" />
          </template>
        </ComboboxInfiniteGenre>
      </v-col>
    </v-row>

    <v-row>
      <v-col>
        <ComboboxInfiniteTag
          v-model="model.tags"
          include="SERIES"
        >
          <template #prepend>
            <LockIcon v-model="model.tagsLock" />
          </template>
        </ComboboxInfiniteTag>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import * as v from 'valibot'
import { vSeriesMetadataDto } from '@/generated/openapi/valibot.gen'
import { useLockWatcher } from '@/composables/form'

const vSeriesUpdateTags = v.pick(vSeriesMetadataDto, ['tags', 'tagsLock', 'genres', 'genresLock'])
type SeriesUpdateTags = v.InferOutput<typeof vSeriesUpdateTags>

const model = defineModel<SeriesUpdateTags>({ required: true })
useLockWatcher(model, vSeriesUpdateTags)
</script>
