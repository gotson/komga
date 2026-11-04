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
      />
    </template>

    <template #[`item.2`]>
      <BookFormBulkContributors
        v-model="model.bulkUpdates"
        :entities="entities"
      />
    </template>

    <template #[`item.2.5`]>
      <BookFormBulkNotableEntities
        v-model="model.bulkUpdates"
        :entities="entities"
      />
    </template>

    <template #[`item.3`]>
      <BookFormBulkTags
        v-model="model.bulkUpdates"
        :entities="entities"
        :entities-extra="entitiesExtra"
        :has-one-shots="hasOneShots"
      />
    </template>

    <template #[`item.5`]>
      <v-sheet
        v-if="model"
        class="pa-4"
      >
        <PosterUpload @upload-queue-changed="(it) => (model!.uploadQueue = it)" />
      </v-sheet>
    </template>

    <template #[`item.6`]>
      <SeriesFormBulkSharing
        v-model="model.bulkUpdates"
        :entities="entitiesExtra"
      />
    </template>
  </v-tabs>
</template>

<script setup lang="ts">
import type { BookDto } from '@/generated/openapi'
import { useIntl } from 'vue-intl'
import type { OneShotAttributesBulk } from '@/types/oneshot'
import { commonMessages } from '@/utils/i18n/common-messages'
import type { BulkUpdateBook, EntitiesUpdateBulk } from '@/types/entityUpdate'

const intl = useIntl()

const model = defineModel<EntitiesUpdateBulk<BookDto, OneShotAttributesBulk, BulkUpdateBook>>({
  required: true,
})

const currentTab = ref(2)

const tabs = computed(() => [
  {
    text: intl.formatMessage(commonMessages.bookFormEditContributors),
    value: 2,
  },
  {
    text: intl.formatMessage(commonMessages.bookFormEditNotableEntities),
    value: 2.5,
  },
  {
    text: intl.formatMessage(commonMessages.bookFormEditTags),
    value: 3,
  },
  {
    text: intl.formatMessage(commonMessages.bookFormEditPoster),
    value: 5,
  },
  ...(hasOneShots.value
    ? [
        {
          text: intl.formatMessage(commonMessages.seriesFormEditSharing),
          value: 6,
        },
      ]
    : []),
])

const entities = computed(() => model.value.entities.map((it) => it.entity.metadata))
const entitiesExtra = computed(() =>
  model.value.entities.flatMap((it) => (it.extra ? [it.extra] : [])),
)
const hasOneShots = computed(() => model.value.entities.some((it) => it.entity.oneshot))
</script>
