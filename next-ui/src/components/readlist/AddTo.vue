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
              description: 'Read list add to: search field label',
              defaultMessage: 'Search or create',
              id: 'icSG40',
            })
          "
          autofocus
        />
        <v-btn
          :text="
            $formatMessage({
              description: 'Read list add to: create button label',
              defaultMessage: 'Create',
              id: '7gdpKG',
            })
          "
          :disabled="isDuplicate || !search"
          :loading="creating"
          @click="createReadList()"
        />
      </div>
    </div>

    <div
      class="flex-grow-1 overflow-y-auto"
      style="min-height: 0"
    >
      <v-list v-if="dataFlat?.length !== 0">
        <v-list-item
          v-for="rl in dataFlat"
          :key="rl.id"
          @click="addToReadList(rl)"
        >
          <template #prepend>
            <v-img
              width="52"
              height="75"
              contain
              :src="readListPosterUrl(rl.id, cacheStore.getVersion(rl.id))"
              lazy-src="@/assets/cover.svg"
              class="me-2"
            />
          </template>

          <v-list-item-title>{{ rl.name }}</v-list-item-title>
          <v-list-item-subtitle
            >{{
              $formatMessage(
                {
                  description: 'Read list add to: read list item subtitle: count of books',
                  defaultMessage: '{count, plural, one {# book} other {# books} }',
                  id: 'DG4tYb',
                },
                {
                  count: rl.bookIds.length,
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
import { useInfiniteQuery, useQuery } from '@pinia/colada'
import {
  readListsListQueryInfinite,
  useCreateReadList,
  useUpdateReadList,
} from '@/colada/readlists'
import { readListPosterUrl } from '@/api/images'
import { refDebounced } from '@vueuse/core'
import { useIntl } from 'vue-intl'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useMessagesStore } from '@/stores/messages'
import type { ReadListDto } from '@/generated/openapi'
import { bookListQuery } from '@/colada/books'
import { PageRequest } from '@/types/PageRequest'
import { useImageCacheStore } from '@/stores/image-cache'

const { bookIds = [], seriesIds = [] } = defineProps<{
  bookIds?: string[]
  seriesIds?: string[]
}>()

const emit = defineEmits<{
  created: [readList: ReadListDto]
  addedTo: [readList: ReadListDto]
}>()

const intl = useIntl()
const messagesStore = useMessagesStore()
const cacheStore = useImageCacheStore()

const search = ref<string>('')
const searchDebounced = refDebounced(search, 500)

const { data, hasNextPage, loadNextPage } = useInfiniteQuery(() =>
  readListsListQueryInfinite({
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
          description: 'Read list add to: duplicate name error message',
          defaultMessage: 'A read list with that name already exists',
          id: 'FBGnzt',
        }),
      ]
    : []),
])

const { data: seriesBooks } = useQuery(() => ({
  ...bookListQuery({
    search: {
      condition: {
        anyOf: seriesIds.map((s) => ({ seriesId: { operator: 'Is', value: s } })),
      },
    },
    pageRequest: PageRequest.Unpaged([
      { key: 'series', order: 'asc' },
      { key: 'metadata.numberSort', order: 'asc' },
    ]),
  }),
  enabled: seriesIds.length > 0,
}))

const effectiveBookIds = computed(() => {
  const seriesBookIds = seriesBooks.value?.content?.map((b) => b.id) ?? []
  return [...bookIds, ...seriesBookIds]
})

const { mutateAsync: postReadList, isLoading: creating } = useCreateReadList()
const { mutateAsync: updateReadList } = useUpdateReadList()

function createReadList() {
  postReadList({
    name: search.value,
    bookIds: effectiveBookIds.value,
    ordered: true,
    summary: '',
  })
    .then((data) => {
      if (data) emit('created', data)
    })
    .catch((error) =>
      messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError),
    )
}

function addToReadList(readList: ReadListDto) {
  updateReadList({
    readListId: readList.id,
    data: { bookIds: [...new Set([...readList.bookIds, ...effectiveBookIds.value])] },
  })
    .then(() => emit('addedTo', readList))
    .catch((error) =>
      messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError),
    )
}
</script>
