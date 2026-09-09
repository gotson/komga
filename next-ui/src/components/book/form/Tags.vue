<template>
  <v-container fluid>
    <v-row v-if="modelOneShot">
      <v-col>
        <ComboboxGenre v-model="modelOneShot.genres">
          <template #prepend>
            <LockIcon v-model="modelOneShot.genresLock" />
          </template>
        </ComboboxGenre>
      </v-col>
    </v-row>

    <v-row>
      <v-col>
        <ComboboxTag
          v-model="model.tags"
          include="BOOK"
        >
          <template #prepend>
            <LockIcon v-model="model.tagsLock" />
          </template>
        </ComboboxTag>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import * as v from 'valibot'
import { vBookMetadataDto, vSeriesMetadataDto } from '@/generated/openapi/valibot.gen'
import { useLockWatcher } from '@/composables/form'

const vBookUpdateTags = v.pick(vBookMetadataDto, ['tags', 'tagsLock'])
type BookUpdateTags = v.InferOutput<typeof vBookUpdateTags>

const model = defineModel<BookUpdateTags>({ required: true })
useLockWatcher(model, vBookUpdateTags)

const vOneShotGenres = v.pick(vSeriesMetadataDto, ['genres', 'genresLock'])
type OneShotGenres = v.InferOutput<typeof vOneShotGenres>

const modelOneShot = defineModel<OneShotGenres>('oneShotAttributes', { required: false })
useLockWatcher(modelOneShot, vOneShotGenres)
</script>
