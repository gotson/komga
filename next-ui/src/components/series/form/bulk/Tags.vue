<template>
  <v-container fluid>
    <v-row>
      <v-col>
        <ComboboxInfiniteBulkGenre
          v-model="model.genres"
          :initial-counts="initialCounts.genres"
          :total-count="entities.length"
        >
          <template #prepend>
            <LockIconBulk
              v-model="model.genresLock"
              :initial-counts="initialCounts.genresLock"
              :total-count="entities.length"
            />
          </template>
        </ComboboxInfiniteBulkGenre>
      </v-col>
    </v-row>

    <v-row>
      <v-col>
        <ComboboxInfiniteBulkTag
          v-model="model.tags"
          :initial-counts="initialCounts.tags"
          :total-count="entities.length"
          include="SERIES"
        >
          <template #prepend>
            <LockIconBulk
              v-model="model.tagsLock"
              :initial-counts="initialCounts.tagsLock"
              :total-count="entities.length"
            />
          </template>
        </ComboboxInfiniteBulkTag>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import type { SeriesMetadataDto } from '@/generated/openapi'
import type { BulkUpdateSeries } from '@/types/entityUpdate'
import { getInitialCountsForKeys } from '@/functions/bulkEdit'

type SeriesBulkUpdateTags = Pick<BulkUpdateSeries, 'genres' | 'genresLock' | 'tags' | 'tagsLock'>

const model = defineModel<SeriesBulkUpdateTags>({ required: true })

const props = defineProps<{
  entities: Pick<SeriesMetadataDto, 'genres' | 'genresLock' | 'tags' | 'tagsLock'>[]
}>()

const initialCounts = getInitialCountsForKeys(props.entities, [
  'genres',
  'genresLock',
  'tags',
  'tagsLock',
])
</script>
