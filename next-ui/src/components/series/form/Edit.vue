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
        rounded="0"
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
      <SeriesFormGeneral
        v-model="model.entity.metadata"
        @update:error-count="(errorCount) => (tabErrors[item.value] = errorCount)"
      />
    </template>

    <template #[`item.2`]="{ item }">
      <SeriesFormTitles
        v-model="model.entity.metadata"
        @update:error-count="(errorCount) => (tabErrors[item.value] = errorCount)"
      />
    </template>

    <template #[`item.3`]>
      <SeriesFormTags v-model="model.entity.metadata" />
    </template>

    <template #[`item.4`]="{ item }">
      <ItemFormLinks
        v-model="model.entity.metadata"
        @update:error-count="(errorCount) => (tabErrors[item.value] = errorCount)"
      />
    </template>

    <template #[`item.5`]>
      <v-sheet class="pa-4">
        <PosterUpload
          :entity-posters="entityPosters ?? []"
          @upload-queue-changed="(it) => (model.uploadQueue = it)"
          @delete-queue-changed="(it) => (model.deleteQueue = it)"
          @poster-selected="(it) => (model.selected = it)"
        />
      </v-sheet>
    </template>

    <template #[`item.6`]>
      <SeriesFormSharing v-model="model.entity.metadata" />
    </template>
  </v-tabs>
</template>

<script setup lang="ts">
import type { SeriesDto } from '@/generated/openapi'
import type { EntityUpdate } from '@/functions/poster'
import { useQuery } from '@pinia/colada'
import { useIntl } from 'vue-intl'
import { seriesPostersQuery } from '@/colada/series'

const intl = useIntl()

const model = defineModel<EntityUpdate<SeriesDto>>({ required: true })
const submitFailed = defineModel<boolean>('submit-failed', { required: false })

const currentTab = ref(1)
const tabErrors = ref<Record<number, number>>({})

const tabs = [
  {
    text: intl.formatMessage({
      description: 'Form edit series: General',
      defaultMessage: 'General',
      id: 'ZPuFVu',
    }),
    value: 1,
  },
  {
    text: intl.formatMessage({
      description: 'Form edit series: alternate titles',
      defaultMessage: 'Alternate titles',
      id: 'zWRhDm',
    }),
    value: 2,
  },
  {
    text: intl.formatMessage({
      description: 'Form edit series: Tags',
      defaultMessage: 'Tags',
      id: 'Epp7Fi',
    }),
    value: 3,
  },
  {
    text: intl.formatMessage({
      description: 'Form edit series: Links',
      defaultMessage: 'Links',
      id: '4U6L0S',
    }),
    value: 4,
  },
  {
    text: intl.formatMessage({
      description: 'Form edit series: Poster',
      defaultMessage: 'Poster',
      id: 'Fdinos',
    }),
    value: 5,
  },
  {
    text: intl.formatMessage({
      description: 'Form edit series: Sharing',
      defaultMessage: 'Sharing',
      id: 'oB2hG3',
    }),
    value: 6,
  },
]

const { data: entityPosters } = useQuery(() =>
  seriesPostersQuery({
    seriesId: model.value.entity.id,
  }),
)

watch(submitFailed, (attempted) => {
  if (attempted) {
    // find first tab with errors
    currentTab.value = Number(Object.entries(tabErrors.value).find(([, value]) => value > 0)?.[0])
    submitFailed.value = false
  }
})
</script>
