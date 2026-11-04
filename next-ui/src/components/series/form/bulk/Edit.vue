<template>
  <v-tabs
    v-model="currentTab"
    :items="tabs"
    show-arrows
  >
    <template #tab="{ item }">
      <v-tab
        :value="item.value"
        :text="item.text"
      >
        <template #append>
          <v-badge
            :model-value="(tabErrors[item.value] ?? 0) > 0"
            :content="tabErrors[item.value]"
            color="error"
            inline
          />
        </template>
      </v-tab>
    </template>

    <template #[`item.1`]="{ item }">
      <SeriesFormBulkGeneral
        v-model="model.bulkUpdates"
        :entities="entities"
        :only-one-shots="hasOneShots === 'all'"
        @update:error-count="(errorCount) => (tabErrors[item.value] = errorCount)"
      />
    </template>

    <template #[`item.3`]>
      <SeriesFormBulkTags
        v-model="model.bulkUpdates"
        :entities="entities"
      />
    </template>

    <template #[`item.5`]>
      <v-sheet class="pa-4">
        <PosterUpload @upload-queue-changed="(it) => (model.uploadQueue = it)" />
      </v-sheet>
    </template>

    <template #[`item.6`]>
      <SeriesFormBulkSharing
        v-model="model.bulkUpdates"
        :entities="entities"
      />
    </template>
  </v-tabs>
</template>

<script setup lang="ts">
import type { SeriesDto } from '@/generated/openapi'
import { useIntl } from 'vue-intl'
import { commonMessages } from '@/utils/i18n/common-messages'
import type { BulkUpdateSeries, EntitiesUpdateBulk } from '@/types/entityUpdate'
import { getBooleanArrayState } from '@/functions/array'

const intl = useIntl()

const model = defineModel<EntitiesUpdateBulk<SeriesDto, never, BulkUpdateSeries>>({
  required: true,
})
const submitFailed = defineModel<boolean>('submit-failed', { required: false })

const currentTab = ref(1)
const tabErrors = ref<Record<number, number>>({})

const tabs = [
  {
    text: intl.formatMessage(commonMessages.seriesFormEditGeneral),
    value: 1,
  },
  {
    text: intl.formatMessage(commonMessages.seriesFormEditTags),
    value: 3,
  },
  {
    text: intl.formatMessage(commonMessages.seriesFormEditPoster),
    value: 5,
  },
  {
    text: intl.formatMessage(commonMessages.seriesFormEditSharing),
    value: 6,
  },
]

watch(submitFailed, (attempted) => {
  if (attempted) {
    // find first tab with errors
    currentTab.value = Number(Object.entries(tabErrors.value).find(([, value]) => value > 0)?.[0])
    submitFailed.value = false
  }
})

const entities = computed(() => model.value.entities.map((it) => it.entity.metadata))
const hasOneShots = computed(() =>
  getBooleanArrayState(model.value.entities.map((it) => it.entity.oneshot)),
)
</script>
