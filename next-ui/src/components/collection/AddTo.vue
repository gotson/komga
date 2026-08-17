<template>
  <div
    class="d-flex flex-column flex-grow-1 overflow-hidden"
    style="min-height: 0"
  >
    <div class="flex-grow-0 flex-shrink-0">
      <div class="d-flex ga-2 align-baseline pt-2">
        <v-text-field
          v-model="search"
          variant="outlined"
          clearable
          :error-messages="errorMessages"
          :label="
            $formatMessage({
              description: 'Collection add to: search field label',
              defaultMessage: 'Search or create',
              id: 'DbrNAW',
            })
          "
          autofocus
        />
        <v-btn
          :text="
            $formatMessage({
              description: 'Collection add to: create button label',
              defaultMessage: 'Create',
              id: '4Ybicn',
            })
          "
          :disabled="isDuplicate || !search"
          :loading="creating"
          @click="createCollection()"
        />
      </div>
    </div>

    <div
      class="flex-grow-1 overflow-y-auto"
      style="min-height: 0"
    >
      <v-list v-if="dataFlat?.length !== 0">
        <v-list-item
          v-for="item in dataFlat"
          :key="item.id"
          @click="addToCollection(item)"
        >
          <template #prepend>
            <v-img
              width="52"
              height="75"
              contain
              :src="collectionPosterUrl(item.id)"
              lazy-src="@/assets/cover.svg"
              class="me-2"
            />
          </template>

          <v-list-item-title>{{ item.name }}</v-list-item-title>
          <v-list-item-subtitle
            >{{
              $formatMessage(
                {
                  description: 'Collection add to: collection item subtitle: count of series',
                  defaultMessage: '{count, plural, one {# series} other {# series} }',
                  id: 'Fwvj/i',
                },
                {
                  count: item.seriesIds.length,
                },
              )
            }}
          </v-list-item-subtitle>
        </v-list-item>
      </v-list>

      <EmptyStateSearchNoResults
        v-if="searchDebounced && dataFlat?.length == 0"
        @reset="search = ''"
      />

      <div
        v-if="hasNextPage"
        v-intersect="(isIntersecting: boolean) => (isIntersecting ? loadNextPage() : undefined)"
        style="min-height: 40px"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useInfiniteQuery } from '@pinia/colada'
import { collectionPosterUrl } from '@/api/images'
import { refDebounced } from '@vueuse/core'
import { useIntl } from 'vue-intl'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useMessagesStore } from '@/stores/messages'
import type { CollectionDto } from '@/generated/openapi'
import {
  collectionsListQueryInfinite,
  useCreateCollection,
  useUpdateCollection,
} from '@/colada/collections'

const { seriesIds = [] } = defineProps<{
  seriesIds?: string[]
}>()

const emit = defineEmits<{
  created: [collection: CollectionDto]
  addedTo: [collection: CollectionDto]
}>()

const intl = useIntl()
const messagesStore = useMessagesStore()

const search = ref<string>('')
const searchDebounced = refDebounced(search, 500)

const { data, hasNextPage, loadNextPage } = useInfiniteQuery(() =>
  collectionsListQueryInfinite({
    search: searchDebounced.value,
    sort: [{ key: 'lastModifiedDate', order: 'desc' }],
  }),
)
const dataFlat = computed(() => data.value?.pages.flatMap((it) => it?.content ?? []))

const isDuplicate = computed(
  () =>
    search.value &&
    dataFlat.value?.some((it) => it.name.toLocaleLowerCase() === search.value.toLocaleLowerCase()),
)

const errorMessages = computed(() => [
  ...(isDuplicate.value
    ? [
        intl.formatMessage({
          description: 'Collection add to: duplicate name error message',
          defaultMessage: 'A collection with that name already exists',
          id: 'vOS8Dl',
        }),
      ]
    : []),
])

const { mutateAsync: postCollection, isLoading: creating } = useCreateCollection()
const { mutateAsync: updateCollection } = useUpdateCollection()

function createCollection() {
  postCollection({
    name: search.value,
    seriesIds: seriesIds,
    ordered: false,
  })
    .then((data) => {
      if (data) emit('created', data)
    })
    .catch((error) =>
      messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError),
    )
}

function addToCollection(collection: CollectionDto) {
  updateCollection({
    collectionId: collection.id,
    data: { seriesIds: [...new Set([...collection.seriesIds, ...seriesIds])] },
  })
    .then(() => emit('addedTo', collection))
    .catch((error) =>
      messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError),
    )
}
</script>
