<template>
  <v-container fluid>
    <v-row v-if="hasOneShots">
      <v-col>
        <ComboboxInfiniteBulkGenre
          v-model="model.genres"
          :initial-counts="initialCountsExtra.genres"
          :total-count="entities.length"
        >
          <template #prepend>
            <LockIconBulk
              v-model="model.genresLock"
              :initial-counts="initialCountsExtra.genresLock"
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
          include="BOOK"
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
import type { BulkUpdateBook } from '@/types/entityUpdate'
import type { BookMetadataDto } from '@/generated/openapi'
import { getInitialCountsForKeys } from '@/functions/bulkEdit'
import type { OneShotAttributesBulk } from '@/types/oneshot'

type BookBulkUpdateTags = Pick<BulkUpdateBook, 'tags' | 'tagsLock' | 'genres' | 'genresLock'>

const model = defineModel<BookBulkUpdateTags>({ required: true })

const props = defineProps<{
  entities: Pick<BookMetadataDto, 'tags' | 'tagsLock'>[]
  entitiesExtra: OneShotAttributesBulk[]
  hasOneShots: boolean
}>()

const initialCounts = getInitialCountsForKeys(props.entities, ['tags', 'tagsLock'])
const initialCountsExtra = getInitialCountsForKeys(props.entitiesExtra, ['genres', 'genresLock'])
</script>
