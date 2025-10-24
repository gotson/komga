<template>
  <v-dialog
    v-model="showDialog"
    :activator="activator"
    :fullscreen="fullscreen"
    :transition="fullscreen ? 'dialog-bottom-transition' : undefined"
    max-width="600px"
    :aria-label="dialogTitle"
    @after-leave="reset()"
  >
    <template #default="{ isActive }">
      <v-card :title="dialogTitle">
        <template #append>
          <v-icon
            icon="i-mdi:close"
            @click="isActive.value = false"
          />
        </template>

        <v-card-text>
          <v-list
            :disabled="isLoading"
            elevation="2"
          >
            <v-progress-linear
              v-if="isLoading"
              indeterminate
              height="3"
              class="position-absolute top-0"
            />

            <v-text-field
              v-model="searchStringRef"
              :label="
                $formatMessage({
                  description: 'Series picker dialog: search field label',
                  defaultMessage: 'Search series',
                  id: 'tPD6YO',
                })
              "
              variant="solo"
              flat
              hide-details
              autofocus
              clearable
            />

            <template v-if="searchStringRef">
              <v-divider class="my-2" />

              <template v-if="series?.content">
                <v-list-item
                  v-for="(s, index) in series.content"
                  :key="index"
                  @click="pick(s)"
                >
                  <template #prepend>
                    <v-img
                      width="52"
                      height="75"
                      contain
                      :src="seriesThumbnailUrl(s.id)"
                      lazy-src="@/assets/cover.svg"
                      class="me-2"
                    />
                  </template>
                  <v-list-item-title>{{ s.metadata.title }}</v-list-item-title>
                  <v-list-item-subtitle
                    >{{ libraries?.find((it) => it.id === s.libraryId)?.name }}
                  </v-list-item-subtitle>
                  <v-list-item-subtitle v-if="s.booksMetadata.releaseDate"
                    >{{
                      $formatDate(s.booksMetadata.releaseDate, { year: 'numeric', timeZone: 'UTC' })
                    }}
                  </v-list-item-subtitle>
                </v-list-item>
              </template>

              <v-empty-state
                v-if="series?.content?.length == 0"
                icon="i-mdi:magnify"
                :title="
                  $formatMessage({
                    description: 'Series picker dialog: no results empty state - title',
                    defaultMessage: 'No series found',
                    id: '4g2M9O',
                  })
                "
                :text="
                  $formatMessage({
                    description: 'Series picker dialog: no results empty state - text',
                    defaultMessage: 'Try searching for something else',
                    id: 'lzHPYD',
                  })
                "
                color="secondary"
              />

              <EmptyStateNetworkError v-if="error" />
            </template>
          </v-list>
        </v-card-text>
      </v-card>
    </template>
  </v-dialog>
</template>

<script setup lang="ts">
import { useQuery } from '@pinia/colada'
import { seriesListQuery } from '@/colada/series'
import type { components } from '@/generated/openapi/komga'
import { seriesThumbnailUrl } from '@/api/images'
import { refDebounced } from '@vueuse/core'
import { useLibraries } from '@/colada/libraries'
import { PageRequest } from '@/types/PageRequest'
import { useIntl } from 'vue-intl'

const showDialog = defineModel<boolean>('dialog', { required: false })
const intl = useIntl()

const {
  includeOneShots = true,
  searchString = '',
  fullscreen = undefined,
  activator = undefined,
} = defineProps<{
  includeOneShots?: boolean
  searchString?: string
  fullscreen?: boolean
  activator?: Element | string
}>()

const emit = defineEmits<{
  selectedSeries: [series: components['schemas']['SeriesDto']]
}>()

const searchStringRef = ref<string>(searchString)

// sync the ref if the prop changes
watch(
  () => searchString,
  (it) => (searchStringRef.value = it),
)

const searchStringDebounced = refDebounced(searchStringRef, 500)
const {
  data: series,
  isLoading,
  error,
} = useQuery(seriesListQuery, () => {
  const search: components['schemas']['SeriesSearch'] = {
    fullTextSearch: searchStringDebounced.value,
    ...(!includeOneShots && {
      condition: {
        oneShot: { operator: 'IsFalse' },
      },
    }),
  }

  return {
    search: search,
    pause: !searchStringDebounced.value,
    pageRequest: PageRequest.Unpaged(),
  }
})

const { data: libraries } = useLibraries()

function pick(selectedSeries: components['schemas']['SeriesDto']) {
  emit('selectedSeries', selectedSeries)
  showDialog.value = false
}

function reset() {
  searchStringRef.value = searchString
}

const dialogTitle = intl.formatMessage({
  description: 'Series picker dialog: title',
  defaultMessage: 'Select series',
  id: 'SIfmpC',
})
</script>
<script setup lang="ts"></script>
