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
      <BookFormGeneral
        v-if="model"
        v-model="model.entity.metadata"
        v-model:one-shot-attributes="model.extra"
        @update:error-count="(errorCount) => (tabErrors[item.value] = errorCount)"
      />
    </template>

    <template #[`item.2`]>
      <BookFormContributors
        v-if="model"
        v-model="model.entity"
      />
    </template>

    <template #[`item.2.5`]>
      <BookFormNotableEntities
        v-if="model"
        v-model="model.entity"
      />
    </template>

    <template #[`item.3`]>
      <BookFormTags
        v-if="model"
        v-model="model.entity.metadata"
        v-model:one-shot-attributes="model.extra"
      />
    </template>

    <template #[`item.4`]="{ item }">
      <ItemFormLinks
        v-if="model"
        v-model="model.entity.metadata"
        @update:error-count="(errorCount) => (tabErrors[item.value] = errorCount)"
      />
    </template>

    <template #[`item.5`]>
      <v-sheet
        v-if="model"
        class="pa-4"
      >
        <PosterUpload
          :entity-posters="entityPosters ?? []"
          @upload-queue-changed="(it) => (model!.uploadQueue = it)"
          @delete-queue-changed="(it) => (model!.deleteQueue = it)"
          @poster-selected="(it) => (model!.selected = it)"
        />
      </v-sheet>
    </template>

    <template #[`item.6`]>
      <SeriesFormSharing
        v-if="model"
        v-model="model.extra"
      />
    </template>
  </v-tabs>
</template>

<script setup lang="ts">
import type { BookDto } from '@/generated/openapi'
import { useQuery } from '@pinia/colada'
import { useIntl } from 'vue-intl'
import { bookPostersQuery } from '@/colada/books'
import type { OneShotAttributes } from '@/types/oneshot'
import { commonMessages } from '@/utils/i18n/common-messages'
import type { EntityUpdate } from '@/types/entityUpdate'

const intl = useIntl()

const model = defineModel<EntityUpdate<BookDto, OneShotAttributes>>()
const submitFailed = defineModel<boolean>('submit-failed', { required: false })

const currentTab = ref(1)
const tabErrors = ref<Record<number, number>>({})

const tabs = computed(() => [
  {
    text: intl.formatMessage({
      description: 'Form edit book: General',
      defaultMessage: 'General',
      id: 'exrB0E',
    }),
    value: 1,
  },
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
    text: intl.formatMessage(commonMessages.bookFormEditLinks),
    value: 4,
  },
  {
    text: intl.formatMessage(commonMessages.bookFormEditPoster),
    value: 5,
  },
  ...(model.value?.extra
    ? [
        {
          text: intl.formatMessage(commonMessages.seriesFormEditSharing),
          value: 6,
        },
      ]
    : []),
])

const { data: entityPosters } = useQuery(() => ({
  ...bookPostersQuery({
    bookId: model.value?.entity.id || 'none',
  }),
  enabled: !!model.value,
}))

watch(submitFailed, (attempted) => {
  if (attempted) {
    // find first tab with errors
    currentTab.value = Number(Object.entries(tabErrors.value).find(([, value]) => value > 0)?.[0])
    submitFailed.value = false
  }
})
</script>
