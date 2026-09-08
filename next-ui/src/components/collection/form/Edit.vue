<template>
  <v-tabs
    v-model="currentTab"
    :items="tabs"
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

    <template #[`item.1`]>
      <CollectionFormGeneral
        v-model="model.entity"
        @update:error-count="(errorCount) => (tabErrors[1] = errorCount)"
      />
    </template>

    <template #[`item.2`]>
      <v-sheet class="pa-4">
        <PosterUpload
          :entity-posters="entityPosters ?? []"
          @upload-queue-changed="(it) => (model.uploadQueue = it)"
          @delete-queue-changed="(it) => (model.deleteQueue = it)"
          @poster-selected="(it) => (model.selected = it)"
        />
      </v-sheet>
    </template>
  </v-tabs>
</template>

<script setup lang="ts">
import type { CollectionDto } from '@/generated/openapi'
import type { EntityUpdate } from '@/functions/poster'
import { useQuery } from '@pinia/colada'
import { collectionPostersQuery } from '@/colada/collections'
import { useIntl } from 'vue-intl'

const intl = useIntl()

const model = defineModel<EntityUpdate<CollectionDto>>({ required: true })
const submitFailed = defineModel<boolean>('submit-failed', { required: false })

const currentTab = ref(1)
const tabErrors = ref<Record<number, number>>({})

const tabs = [
  {
    text: intl.formatMessage({
      description: 'Form edit collection: General',
      defaultMessage: 'General',
      id: 'ddpX9L',
    }),
    value: 1,
  },
  {
    text: intl.formatMessage({
      description: 'Form edit collection: Poster',
      defaultMessage: 'Poster',
      id: 'ZnnG58',
    }),
    value: 2,
  },
]

const { data: entityPosters } = useQuery(() =>
  collectionPostersQuery({
    collectionId: model.value.entity.id,
  }),
)

watch(submitFailed, (attempted) => {
  if (attempted) {
    currentTab.value = 1
    submitFailed.value = false
  }
})
</script>
