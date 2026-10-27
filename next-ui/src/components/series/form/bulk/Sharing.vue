<template>
  <v-container fluid>
    <v-row>
      <v-col>
        <ComboboxInfiniteBulkSharingLabel
          v-model="model.sharingLabels"
          :initial-counts="initialCounts.sharingLabels"
          :total-count="entities.length"
        >
          <template #prepend>
            <LockIconBulk
              v-model="model.sharingLabelsLock"
              :initial-counts="initialCounts.sharingLabelsLock"
              :total-count="entities.length"
            />
          </template>
        </ComboboxInfiniteBulkSharingLabel>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import type { SeriesMetadataDto } from '@/generated/openapi'
import type { BulkUpdateSeries } from '@/types/entityUpdate'
import { getInitialCountsForKeys } from '@/functions/bulkEdit'

type SeriesBulkUpdateSharing = Pick<BulkUpdateSeries, 'sharingLabels' | 'sharingLabelsLock'>

const model = defineModel<SeriesBulkUpdateSharing>({ required: true })

const props = defineProps<{
  entities: Pick<SeriesMetadataDto, 'sharingLabels' | 'sharingLabelsLock'>[]
}>()

const initialCounts = getInitialCountsForKeys(props.entities, [
  'sharingLabels',
  'sharingLabelsLock',
])
</script>
