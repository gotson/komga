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
      <BookFormGeneral
        v-model="model.entity.metadata"
        v-model:one-shot-attributes="model.extra"
        @update:error-count="(errorCount) => (tabErrors[item.value] = errorCount)"
      />
    </template>

    <template #[`item.2`]>
      <BookFormContributors v-model="model.entity" />
    </template>

    <template #[`item.3`]>
      <BookFormTags
        v-model="model.entity.metadata"
        v-model:one-shot-attributes="model.extra"
      />
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
  </v-tabs>
</template>

<script setup lang="ts">
import type { BookDto } from '@/generated/openapi'
import type { EntityUpdate } from '@/functions/poster'
import { useQuery } from '@pinia/colada'
import { useIntl } from 'vue-intl'
import { bookPostersQuery } from '@/colada/books'
import type { OneShotAttributes } from '@/types/oneshot'

const intl = useIntl()

const model = defineModel<EntityUpdate<BookDto, OneShotAttributes>>({ required: true })
const submitFailed = defineModel<boolean>('submit-failed', { required: false })

const currentTab = ref(1)
const tabErrors = ref<Record<number, number>>({})

const tabs = [
  {
    text: intl.formatMessage({
      description: 'Form edit book: General',
      defaultMessage: 'General',
      id: 'exrB0E',
    }),
    value: 1,
  },
  {
    text: intl.formatMessage({
      description: 'Form edit book: Contributors',
      defaultMessage: 'Contributors',
      id: 'CqlsIa',
    }),
    value: 2,
  },
  {
    text: intl.formatMessage({
      description: 'Form edit book: Tags',
      defaultMessage: 'Tags',
      id: 'UFZ+PY',
    }),
    value: 3,
  },
  {
    text: intl.formatMessage({
      description: 'Form edit book: Links',
      defaultMessage: 'Links',
      id: 'VeEnkD',
    }),
    value: 4,
  },
  {
    text: intl.formatMessage({
      description: 'Form edit book: Poster',
      defaultMessage: 'Poster',
      id: 'xjHL5K',
    }),
    value: 5,
  },
]

const { data: entityPosters } = useQuery(() =>
  bookPostersQuery({
    bookId: model.value.entity.id,
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
