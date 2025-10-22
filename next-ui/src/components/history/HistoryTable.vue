<template>
  <v-data-table-server
    v-model:sort-by="sortBy"
    :loading="isLoading"
    :items="data?.content"
    :items-length="data?.totalElements || 0"
    :items-per-page-options="[
      { value: 10, title: '10' },
      { value: 25, title: '25' },
      { value: 50, title: '50' },
      { value: 100, title: '100' },
    ]"
    :headers="headers"
    fixed-header
    fixed-footer
    multi-sort
    mobile-breakpoint="md"
    show-expand
    style="height: 100%"
    @update:options="updateOptions"
  >
    <template #top>
      <v-toolbar flat>
        <v-toolbar-title>
          <v-icon
            color="medium-emphasis"
            icon="i-mdi:history"
            size="x-small"
            start
          />
          {{
            $formatMessage({
              description: 'History table global header',
              defaultMessage: 'History',
              id: '7z91nm',
            })
          }}
        </v-toolbar-title>
      </v-toolbar>
    </template>

    <template #no-data>
      <EmptyStateNetworkError v-if="error" />
      <template v-else>
        {{
          $formatMessage({
            description: 'History table: shown when table has no data',
            defaultMessage: 'No recent events',
            id: 'Ym48fa',
          })
        }}
      </template>
    </template>

    <template #[`item.type`]="{ value }">
      <div class="d-flex flex-row-reverse flex-md-row ga-2">
        <v-icon :icon="getIcon(value)" />
        <span>{{ $formatMessage(getEventMessage(value)) }}</span>
      </div>
    </template>

    <template #[`item.seriesId`]="{ value: seriesId }">
      {{ seriesCache[seriesId] || seriesId }}
    </template>

    <template #[`item.bookId`]="{ value: bookId }">
      {{ booksCache[bookId] || bookId }}
    </template>

    <template #[`item.timestamp`]="{ value }">
      {{ $formatDate(value, { dateStyle: 'medium', timeStyle: 'short' }) }}
    </template>

    <template #expanded-row="{ columns, item }">
      <tr>
        <td
          :colspan="columns.length"
          class="py-2 px-1 px-md-4"
        >
          <component
            :is="getExpandedComponent(item.type)"
            :event="item"
          />
        </td>
      </tr>
    </template>
  </v-data-table-server>
</template>

<script setup lang="ts">
import { useIntl } from 'vue-intl'
import { PageRequest, type SortItem } from '@/types/PageRequest'
import { useQuery } from '@pinia/colada'
import { historyQuery } from '@/colada/history'
import HistoryExpandBookFileDeleted from '@/components/history/expand/BookFileDeleted.vue'
import HistoryExpandBookImported from '@/components/history/expand/BookImported.vue'
import HistoryExpandBookConverted from '@/components/history/expand/BookConverted.vue'
import HistoryExpandDuplicatePageDeleted from '@/components/history/expand/DuplicatePageDeleted.vue'
import HistoryExpandSeriesDirectoryDeleted from '@/components/history/expand/SeriesDirectoryDeleted.vue'
import { historicalEventMessages } from '@/utils/i18n/enum/historical-event'
import type { MessageDescriptor } from '@formatjs/intl/src/types'
import { seriesDetailQuery } from '@/colada/series'
import { bookDetailQuery } from '@/colada/books'
import { useMemoize } from '@vueuse/core'

const intl = useIntl()

const sortBy = ref<SortItem[]>([{ key: 'timestamp', order: 'desc' }])

const headers = [
  {
    title: intl.formatMessage({
      description: 'History Table table header: Event type',
      defaultMessage: 'Type',
      id: '6nCIPn',
    }),
    key: 'type',
  },
  {
    title: intl.formatMessage({
      description: 'History Table table header: Series',
      defaultMessage: 'Series',
      id: 'pR3VdQ',
    }),
    key: 'seriesId',
  },
  {
    title: intl.formatMessage({
      description: 'History Table table header: Book',
      defaultMessage: 'Book',
      id: 'zqAxRE',
    }),
    key: 'bookId',
  },
  {
    title: intl.formatMessage({
      description: 'History Table table header: Date Time',
      defaultMessage: 'Date Time',
      id: 'HMbMJF',
    }),
    key: 'timestamp',
  },
]

const pageRequest = ref<PageRequest>(new PageRequest())

const { data, isLoading, error } = useQuery(historyQuery, () => ({ ...pageRequest.value }))

function updateOptions({
  page,
  itemsPerPage,
  sortBy,
}: {
  page: number
  itemsPerPage: number
  sortBy: SortItem[]
}) {
  pageRequest.value = PageRequest.FromVuetify(page - 1, itemsPerPage, sortBy)
}

function getIcon(eventType: string): string {
  switch (eventType) {
    case 'BookFileDeleted':
      return 'i-mdi:file-remove'
    case 'SeriesFolderDeleted':
      return 'i-mdi:folder-remove'
    case 'DuplicatePageDeleted':
      return 'i-mdi:book-minus'
    case 'BookConverted':
      return 'i-mdi:archive-refresh'
    case 'BookImported':
      return 'i-mdi:import'
    default:
      return ''
  }
}

function getEventMessage(eventType: string): MessageDescriptor {
  return (
    historicalEventMessages[eventType] || {
      id: eventType,
      defaultMessage: eventType,
    }
  )
}

function getExpandedComponent(eventType: string): Component | null {
  switch (eventType) {
    case 'BookFileDeleted':
      return markRaw(HistoryExpandBookFileDeleted)
    case 'SeriesFolderDeleted':
      return markRaw(HistoryExpandSeriesDirectoryDeleted)
    case 'DuplicatePageDeleted':
      return markRaw(HistoryExpandDuplicatePageDeleted)
    case 'BookConverted':
      return markRaw(HistoryExpandBookConverted)
    case 'BookImported':
      return markRaw(HistoryExpandBookImported)
    default:
      return null
  }
}

const seriesCache = reactive<Record<string, string | undefined>>({})
const booksCache = reactive<Record<string, string | undefined>>({})

const getSeriesTitle = useMemoize(async (seriesId: string) =>
  useQuery(seriesDetailQuery, () => ({ seriesId: seriesId }))
    .refresh(true)
    .then(({ data }) => data?.metadata.title)
    .catch(() => undefined),
)

const getBookTitle = useMemoize(async (bookId: string) =>
  useQuery(bookDetailQuery, () => ({ bookId: bookId }))
    .refresh(true)
    .then(({ data }) => data?.metadata.title)
    .catch(() => undefined),
)

watch(data, (data) => {
  for (const seriesId of new Set(data?.content?.map((s) => s.seriesId))) {
    if (seriesId) {
      void getSeriesTitle(seriesId).then((title) => (seriesCache[seriesId] = title))
    }
  }

  for (const bookId of new Set(data?.content?.map((s) => s.bookId))) {
    if (bookId) {
      void getBookTitle(bookId).then((title) => (booksCache[bookId] = title))
    }
  }
})
</script>
