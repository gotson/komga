<template>
  <v-data-table
    v-model="selectedBookIds"
    v-model:items-per-page="itemsPerPage"
    :loading="importing || loading"
    :items="importBooks"
    item-value="transientBook.id"
    :headers="headers"
    :items-per-page-options="itemsPerPageOptions"
    :hide-default-footer="hideFooter"
    fixed-header
    fixed-footer
    show-select
    item-selectable="selectable"
    select-strategy="page"
    mobile-breakpoint="md"
    @update:current-items="onDisplayedItems"
  >
    <template #no-data>
      <v-empty-state
        icon="i-mdi:book-search"
        :title="
          $formatMessage({
            description: 'Import books table: shown when table has no data - title',
            defaultMessage: 'No books found',
            id: 'VvjZEl',
          })
        "
        :text="
          $formatMessage({
            description: 'Import books table: shown when table has no data - subtitle',
            defaultMessage: 'Try scanning another directory',
            id: '9VuyZU',
          })
        "
      />
    </template>

    <template #[`header.analysisStatus`]>
      <v-icon icon="i-mdi:file-check-outline" />
    </template>

    <template #[`item.analysisStatus`]="{ item }: { item: BookImport }">
      <v-progress-circular
        v-if="item.transientBook.status === MediaStatus.UNKNOWN.valueOf()"
        indeterminate
        color="primary"
        :size="20"
        :width="2"
      />
      <v-icon
        v-if="
          item.transientBook.status === MediaStatus.ERROR.valueOf() ||
          item.transientBook.status === MediaStatus.UNSUPPORTED.valueOf()
        "
        v-tooltip="convertErrorCodes(item.transientBook.comment)"
        icon="i-mdi:alert-circle"
        color="error"
      />
      <v-icon
        v-if="item.transientBook.status === MediaStatus.READY.valueOf()"
        icon="i-mdi:check-circle"
        color="success"
      />
    </template>

    <template #[`item.transientBook.name`]="{ value, item }">
      <span :class="item.imported ? 'text-disabled' : undefined">{{ value }}</span>
    </template>

    <template #[`item.series`]="{ item, internalItem, isSelected }">
      <div
        :class="item.selectable ? 'cursor-pointer' : 'cursor-not-allowed'"
        @mouseenter="
          item.selectable
            ? (dialogSeriesPickerActivator = $event.currentTarget as Element)
            : (dialogSeriesPickerActivator = undefined)
        "
        @click="item.selectable ? selectSeriesForOne(item) : undefined"
      >
        <span
          v-if="item.series"
          :class="item.imported ? 'text-disabled' : undefined"
          >{{ item.series?.metadata.title }}</span
        >
        <template v-else>
          <div
            style="height: 2em"
            :class="isSelected(internalItem) ? 'missing' : ''"
          />
        </template>
      </div>
    </template>

    <template #[`item.upgradeBook`]="{ item }">
      <div
        @mouseenter="
          item.upgradable
            ? (dialogBookPickerActivator = $event.currentTarget as Element)
            : undefined
        "
        @click="item.upgradable ? (currentActionedItems = [item]) : undefined"
      >
        <v-chip
          v-if="item.upgradeBook"
          variant="text"
          closable
          class="cursor-pointer"
          :disabled="!item.upgradable"
          @click:close="unassignBook(item)"
        >
          {{ item.upgradeBook.metadata.number }} - {{ item.upgradeBook.metadata.title }}
        </v-chip>
        <v-btn
          v-else
          color=""
          size="small"
          :disabled="!item.upgradable"
          prepend-icon="i-mdi:file-replace-outline"
          :text="
            $formatMessage({
              description: 'Import books table: book upgrade button',
              defaultMessage: 'Upgrade',
              id: 'hrh5Rn',
            })
          "
        />
      </div>
    </template>

    <template #[`item.destinationName`]="{ item }">
      <div
        :class="
          (item.selectable ? 'cursor-pointer' : 'cursor-not-allowed') +
          ' ' +
          (item.imported ? 'text-disabled' : undefined)
        "
        @mouseenter="
          item.selectable
            ? (dialogFileNamePickerActivator = $event.currentTarget as Element)
            : (dialogFileNamePickerActivator = undefined)
        "
        @click="item.selectable ? (currentActionedItems = [item]) : undefined"
      >
        {{ item.destinationName }}
      </div>
    </template>

    <template #[`header.statusMessage`]>
      <v-icon icon="i-mdi:alert-circle-outline" />
    </template>

    <template #[`item.statusMessage`]="{ item, value, internalItem, isSelected }">
      <v-icon
        v-if="item.imported"
        v-tooltip="value"
        icon="i-mdi:import"
        color="info"
      />
      <template v-else-if="isSelected(internalItem)">
        <v-icon
          v-if="item.upgradeBook"
          v-tooltip="value"
          icon="i-mdi:file-replace"
          color="warning"
        />
        <v-icon
          v-else-if="value"
          v-tooltip="value"
          icon="i-mdi:alert-circle"
          color="error"
        />
      </template>
    </template>
  </v-data-table>

  <v-container fluid>
    <v-row
      justify="space-between"
      align="center"
    >
      <v-col cols="auto">
        <v-select
          v-model="copyMode"
          :label="
            $formatMessage({
              description: 'Import books table: bottom bar: import mode selection dropdown label',
              defaultMessage: 'Import mode',
              id: '14/Uh8',
            })
          "
          hide-details
          :items="copyOptions"
          variant="outlined"
          min-width="250"
        ></v-select>
      </v-col>

      <v-col>
        <v-btn
          :text="
            $formatMessage({
              description: 'Import books table: bottom bar: select series button',
              defaultMessage: 'Select series',
              id: 'SqZoei',
            })
          "
          color=""
          :disabled="loading || selectedBookIds.length == 0"
          @mouseenter="dialogSeriesPickerActivator = $event.currentTarget as Element"
          @click="selectSeriesForSelected()"
        />
      </v-col>

      <v-col cols="auto">
        <v-btn
          color="primary"
          :text="
            $formatMessage({
              description: 'Import books table: bottom bar: import button',
              defaultMessage: 'Import',
              id: 'RHJo8j',
            })
          "
          :disabled="importing || loading || importBatch.books.length == 0"
          @click="doImportBooks"
        />
      </v-col>
    </v-row>
  </v-container>

  <DialogSeriesPicker
    :activator="dialogSeriesPickerActivator"
    :fullscreen="display.xs.value"
    :include-one-shots="dialogSeriesIncludeOneShots"
    @selected-series="(series) => seriesPicked(series)"
  />

  <DialogBookPicker
    :activator="dialogBookPickerActivator"
    :fullscreen="display.xs.value"
    :books="currentActionedItems?.at(0)?.seriesBooks"
    @selected-book="(book) => bookPicked(book)"
  />

  <DialogFileNamePicker
    :activator="dialogFileNamePickerActivator"
    :fullscreen="display.xs.value"
    :existing-name="currentActionedItems?.at(0)?.destinationName"
    :series-books="currentActionedItems?.at(0)?.seriesBooks"
    @selected-name="(name) => fileNamePicked(name)"
  />
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { useIntl } from 'vue-intl'
import { MediaStatus } from '@/types/MediaStatus'
import { useErrorCodeFormatter } from '@/composables/errorCodeFormatter'
import { syncRefs, useArrayFilter, useArrayMap } from '@vueuse/core'
import { useDisplay } from 'vuetify'
import { useMutation, useQuery } from '@pinia/colada'
import { seriesDetailQuery } from '@/colada/series'
import { bookListQuery } from '@/colada/books'
import { transientBookAnalyze } from '@/colada/transient-books'
import { type ErrorCause, komgaClient } from '@/api/komga-client'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useMessagesStore } from '@/stores/messages'

class BookImport {
  transientBook: components['schemas']['TransientBookDto']
  destinationName: string
  series?: components['schemas']['SeriesDto']
  seriesBooks?: components['schemas']['BookDto'][]
  upgradeBook?: components['schemas']['BookDto']
  imported: boolean

  constructor(transientBook: components['schemas']['TransientBookDto']) {
    this.transientBook = transientBook
    this.destinationName = transientBook.name
    this.imported = false
  }

  /**
   * Whether the book is selectable.
   * Only books in READY status and not yet imported can be selected
   */
  public get selectable(): boolean {
    return this.transientBook.status === MediaStatus.READY.valueOf() && !this.imported
  }

  public get upgradable(): boolean {
    return this.selectable && !!this.series && !!this.seriesBooks
  }

  public get importable(): boolean {
    return this.selectable && !!this.series
  }

  public get statusMessage(): string {
    switch (this.transientBook.status) {
      case MediaStatus.UNKNOWN.valueOf():
        return intl.formatMessage({
          description: 'Import books: status message: book needs to be analyzed first',
          defaultMessage: 'Book needs to be analyzed first',
          id: 'CPMLrI',
        })
      case MediaStatus.UNSUPPORTED.valueOf():
        return intl.formatMessage({
          description: 'Import books: status message: book format is not supported',
          defaultMessage: 'Book format is not supported',
          id: 'g2UW+6',
        })
      case MediaStatus.ERROR.valueOf():
        return intl.formatMessage({
          description: 'Import books: status message: book could not be analyzed',
          defaultMessage: 'Book could not be analyzed',
          id: '8jE3eP',
        })
    }
    if (!this.series)
      return intl.formatMessage({
        description: 'Import books: status message: choose a series',
        defaultMessage: 'Choose a series',
        id: 'cM9FuW',
      })
    if (this.imported)
      return intl.formatMessage({
        description: 'Import books: status message: import requested',
        defaultMessage: 'Import requested',
        id: 'YHxouG',
      })
    if (this.upgradeBook)
      return intl.formatMessage({
        description: 'Import books: status message: book will be upgraded',
        defaultMessage: 'Book will be upgraded',
        id: 'UoaxO7',
      })
    return ''
  }
}

const messagesStore = useMessagesStore()
const display = useDisplay()
const intl = useIntl()
const { convertErrorCodes } = useErrorCodeFormatter()

const { books = [], loading = false } = defineProps<{
  books?: components['schemas']['TransientBookDto'][]
  loading?: boolean
}>()

// a read-only array of BookImport, kept in sync with the books prop
const importBooksRO = useArrayMap(
  toRef(() => books),
  (it) => new BookImport(it),
)
// read-write array of BookImport, will be modified by the different actions
const importBooks = ref<BookImport[]>([])
// 1-way sync from importBooksRO to importBooks
syncRefs(importBooksRO, importBooks)

// the current items being acted upon, used for dialog callback
const currentActionedItems = ref<BookImport[]>()
// the selected book IDs, used to programmatically select items
const selectedBookIds = ref<string[]>([])
// the selected books
const selectedBooks = useArrayFilter(importBooks, (it) =>
  selectedBookIds.value.includes(it.transientBook.id),
)
const selectedImportableBooks = useArrayFilter(
  importBooks,
  (it) => selectedBookIds.value.includes(it.transientBook.id) && it.importable,
)

const importBatch = computed(
  () =>
    ({
      copyMode: copyMode.value,
      books: selectedImportableBooks.value.map((it) => ({
        destinationName: it.destinationName,
        seriesId: it.series?.id,
        sourceFile: it.transientBook.url,
        upgradeBookId: it.upgradeBook?.id,
      })),
    }) as components['schemas']['BookImportBatchDto'],
)

// only analyze books that are shown
function onDisplayedItems(items: { key: string }[]) {
  importBooks.value
    .filter((b) => items.map((it) => it.key).includes(b.transientBook.id))
    .forEach((b) => {
      if (b.transientBook.status === MediaStatus.UNKNOWN.valueOf()) {
        analyzeBook(b)
      }
    })
}

//region Table setup
const itemsPerPage = ref<number>(display.smAndDown.value ? 1 : 10)
const itemsPerPageOptions = computed(() => (display.smAndDown.value ? [1, 5] : [10, 25, 50]))
const hideFooter = computed(() => importBooks.value.length < (display.smAndDown.value ? 1 : 10))

const headers = [
  {
    title: intl.formatMessage({
      description: 'Import books table header: analysis status',
      defaultMessage: 'Analysis status',
      id: 'f1fW81',
    }),
    key: 'analysisStatus',
    align: 'end',
  },
  {
    title: intl.formatMessage({
      description: 'Import books table header: file name',
      defaultMessage: 'File name',
      id: 'kYDPt1',
    }),
    key: 'transientBook.name',
  },
  {
    title: intl.formatMessage({
      description: 'Import books table header: series',
      defaultMessage: 'Series',
      id: '3OgH93',
    }),
    key: 'series',
  },
  {
    title: intl.formatMessage({
      description: 'Import books table header: book',
      defaultMessage: 'Book',
      id: 'Kie8HQ',
    }),
    key: 'upgradeBook',
  },
  {
    title: intl.formatMessage({
      description: 'Import books table header: destination name',
      defaultMessage: 'Destination name',
      id: 'bSoeY6',
    }),
    key: 'destinationName',
  },
  {
    title: intl.formatMessage({
      description: 'Import books table header: status message',
      defaultMessage: 'Status',
      id: 'knm6Z+',
    }),
    key: 'statusMessage',
    align: 'end',
  },
] as const // workaround for https://github.com/vuetifyjs/vuetify/issues/18901
//endregion

//region Copy Options
const copyOptions = [
  {
    title: intl.formatMessage({
      description: 'Import books: copy option: hardlink/copy',
      defaultMessage: 'Hardlink/Copy files',
      id: 'ap/Lnw',
    }),
    value: 'HARDLINK',
  },
  {
    title: intl.formatMessage({
      description: 'Import books: copy option: move',
      defaultMessage: 'Move files',
      id: 'EMhLEN',
    }),
    value: 'MOVE',
  },
]
const copyMode = ref<string>(copyOptions[0]!.value)
//endregion

//region Series Picker Dialog
const dialogSeriesPickerActivator = ref<Element | undefined>(undefined)
const dialogSeriesIncludeOneShots = ref<boolean>(true)

function seriesPicked(series: components['schemas']['SeriesDto']) {
  if (currentActionedItems.value) {
    currentActionedItems.value.forEach((it) => assignSeries(it, series))
  }
}

function selectSeriesForSelected() {
  dialogSeriesIncludeOneShots.value = false
  currentActionedItems.value = selectedBooks.value
}

function selectSeriesForOne(item: BookImport) {
  dialogSeriesIncludeOneShots.value = true
  currentActionedItems.value = [item]
}
//endregion

//region Book Picker Dialog
const dialogBookPickerActivator = ref<Element | undefined>(undefined)

function bookPicked(book: components['schemas']['BookDto']) {
  if (currentActionedItems.value) {
    currentActionedItems.value.forEach((it) => assignBookNumber(it, book.metadata.numberSort))
  }
}
//endregion

//region File Name Picker dialog
const dialogFileNamePickerActivator = ref<Element | undefined>(undefined)

function fileNamePicked(name: string) {
  if (currentActionedItems.value) {
    currentActionedItems.value.forEach((it) => (it.destinationName = name))
  }
}
//endregion

function analyzeBook(book: BookImport) {
  const { refresh } = useQuery(transientBookAnalyze, () => ({
    transientBookId: book.transientBook.id,
  }))
  void refresh().then(({ data }) => {
    if (data) {
      book.transientBook = data
      if (book.transientBook.seriesId && book.transientBook.seriesId !== book.series?.id)
        fetchSeries(book)
    }
  })
}

function fetchSeries(book: BookImport) {
  const { refresh } = useQuery(seriesDetailQuery, () => ({
    seriesId: book.transientBook.seriesId!,
  }))
  void refresh().then(({ data }) => {
    if (data) {
      assignSeries(book, data)
    }
  })
}

function fetchBooks(book: BookImport) {
  const { refresh } = useQuery(bookListQuery, () => ({
    search: {
      condition: {
        seriesId: { operator: 'Is', value: book.series!.id },
      },
    } as components['schemas']['BookSearch'],
  }))
  void refresh().then(({ data }) => {
    if (data) {
      book.seriesBooks = data.content
      if (book.transientBook.number) assignBookNumber(book, book.transientBook.number)
    }
  })
}

function assignSeries(book: BookImport, series: components['schemas']['SeriesDto']) {
  book.series = series
  fetchBooks(book)
  if (book.importable && !selectedBookIds.value.includes(book.transientBook.id))
    selectedBookIds.value.push(book.transientBook.id)
}

function assignBookNumber(book: BookImport, number: number) {
  book.upgradeBook = book.seriesBooks?.find((b) => b.metadata.numberSort === number)
}

function unassignBook(book: BookImport) {
  book.upgradeBook = undefined
}

const importing = ref<boolean>(false)

function doImportBooks() {
  importing.value = true
  const { mutateAsync } = useMutation({
    mutation: () => komgaClient.POST('/api/v1/books/import', { body: importBatch.value }),
  })

  mutateAsync()
    .then(() => {
      selectedImportableBooks.value.forEach((it) => {
        it.imported = true
        // remove imported books from selection
        if (selectedBookIds.value.includes(it.transientBook.id))
          selectedBookIds.value.splice(selectedBookIds.value.indexOf(it.transientBook.id), 1)
      })
    })
    .catch((error) => {
      messagesStore.messages.push({
        text:
          (error?.cause as ErrorCause)?.message || intl.formatMessage(commonMessages.networkError),
      })
    })
    .finally(() => {
      importing.value = false
    })
}
</script>

<style scoped>
.missing {
  border: 2px dashed red;
}
</style>
