<template>
  <v-container fluid>
    <v-row v-if="model">
      <v-col>
        <ComboboxSharingLabel v-model="model.sharingLabels">
          <template #prepend>
            <LockIcon v-model="model.sharingLabelsLock" />
          </template>
        </ComboboxSharingLabel>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import * as v from 'valibot'
import { vSeriesMetadataDto } from '@/generated/openapi/valibot.gen'
import { useLockWatcher } from '@/composables/form'

const vSeriesUpdateSharing = v.pick(vSeriesMetadataDto, ['sharingLabels', 'sharingLabelsLock'])
type SeriesUpdateSharing = v.InferOutput<typeof vSeriesUpdateSharing>

const model = defineModel<SeriesUpdateSharing>()
useLockWatcher(model, vSeriesUpdateSharing)
</script>
