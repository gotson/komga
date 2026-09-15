<template>
  <v-data-table
    v-model="selectedBookIds"
    :loading="importing || loading"
    :items="importBooks"
    item-value="transientBook.id"
    :headers="headers"
    :hide-default-footer="hideFooter"
    fixed-header
    fixed-footer
    show-select
    item-selectable="selectable"
    select-strategy="page"
    mobile-breakpoint="md"
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

    <template #[`item.analysisStatus`]="{ item }">
      <v-progress-circular
        v-if="item.transientBook.status === MediaStatus.Unknown"
        indeterminate
        color="primary"
        :size="20"
        :width="2"
      />
      <v-icon
        v-if="
          item.transientBook.status === MediaStatus.Error ||
          item.transientBook.status === MediaStatus.Unsupported
        "
        v-ktooltip="convertErrorCodes(item.transientBook.comment)"
        icon="i-mdi:alert-circle"
        color="error"
      />
      <v-icon
        v-if="item.transientBook.status === MediaStatus.Ready"
        icon="i-mdi:check-circle"
        color="success"
      />
      <ImportBooksTransientBookWatcher
        :book-import="item"
        @update:transient-book="(book) => (item.transientBook = book)"
        @update:series="(series) => (item.series = series)"
        @update:series-books="(seriesBooks) => (item.seriesBooks = seriesBooks)"
        @update:upgrade-book="(book) => (item.upgradeBook = book)"
        @update:upgrade-book-pages="(pages) => (item.upgradeBookPages = pages)"
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
        <v-btn
          v-else
          :text="
            $formatMessage({
              description: 'Import books table: series cell: select series button',
              defaultMessage: 'Select series',
              id: '464rk3',
            })
          "
          :disabled="!item.selectable"
          :color="isSelected(internalItem) ? 'error' : ''"
          size="small"
        />
      </div>
    </template>

    <template #[`item.upgradeBook`]="{ item }">
      <div v-if="item.series?.oneshot">
        <v-chip
          :text="
            $formatMessage({
              description: 'Import books table: book cell: one shot indicator',
              defaultMessage: 'One shot',
              id: 'nOcpr8',
            })
          "
          rounded
          :disabled="!item.upgradable"
          color="primary"
          variant="tonal"
          class="text-wrap"
        />
      </div>
      <div
        v-else
        @mouseenter="
          item.upgradable
            ? (dialogBookPickerActivator = $event.currentTarget as Element)
            : undefined
        "
        @click="item.upgradable ? (currentActionedItems = [item]) : undefined"
      >
        <div v-if="item.upgradeBook">
          <v-chip
            v-ktooltip:bottom="
              `${item.upgradeBook.metadata.number} - ${item.upgradeBook.metadata.title}`
            "
            variant="text"
            closable
            class="cursor-pointer"
            :disabled="!item.upgradable"
            @click:close="unassignBook(item)"
          >
            <span
              class="text-truncate"
              style="max-width: 120px"
              >{{ item.upgradeBook.metadata.number }} - {{ item.upgradeBook.metadata.title }}</span
            >
          </v-chip>
        </div>
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

    <template #[`header.details`]>
      <v-icon icon="i-mdi:file-document-outline" />
    </template>

    <template #[`item.details`]="{ item }">
      <v-icon-btn
        v-if="item.upgradeBook"
        v-ktooltip:bottom="
          $formatMessage({
            description: 'Import books table: book compare button',
            defaultMessage: 'Compare',
            id: 'pvD6TS',
          })
        "
        :disabled="item.imported"
        icon="i-mdi:file-compare"
        variant="elevated"
        @mouseenter="compareBookActivator = $event.currentTarget as Element"
        @click="compareBooks(item)"
      />
      <v-icon-btn
        v-else
        v-ktooltip:bottom="
          $formatMessage({
            description: 'Import books table: book details button',
            defaultMessage: 'Show details',
            id: '2LLpzV',
          })
        "
        :disabled="item.imported"
        icon="i-mdi:file-document-outline"
        variant="elevated"
        @mouseenter="bookDetailsActivator = $event.currentTarget as Element"
        @click="bookDetails(item)"
      />
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
        v-ktooltip="$formatMessage(value)"
        icon="i-mdi:import"
        color="info"
      />
      <template v-else-if="isSelected(internalItem)">
        <v-icon
          v-if="item.upgradeBook"
          v-ktooltip="$formatMessage(value)"
          icon="i-mdi:file-replace"
          color="warning"
        />
        <v-icon
          v-else-if="value"
          v-ktooltip="$formatMessage(value)"
          icon="i-mdi:alert-circle"
          color="error"
        />
      </template>
    </template>
  </v-data-table>

  <v-container fluid>
    <v-row class="align-center justify-space-between">
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
    :original-name="currentActionedItems?.at(0)?.originalName"
    :existing-name="currentActionedItems?.at(0)?.destinationName"
    :series-books="currentActionedItems?.at(0)?.seriesBooks"
    @selected-name="(name) => fileNamePicked(name)"
  />
</template>

<script setup lang="ts">
import { useIntl } from 'vue-intl'
import { useErrorCodeFormatter } from '@/composables/errorCodeFormatter'
import { useArrayFilter } from '@vueuse/core'
import { useDisplay } from 'vuetify'
import { useMutation } from '@pinia/colada'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useMessagesStore } from '@/stores/messages'
import {
  type BookDto,
  type BookImportBatchDto,
  komgaImportBooks,
  type SeriesDto,
  type TransientBookDto,
} from '@/generated/openapi'
import { MediaStatus } from '@/types/MediaStatus'
import { useCompareBookDialog } from '@/composables/book/useCompareBookDialog'
import { BookImport } from '@/types/BookImport'
import { useBookDetailsDialog } from '@/composables/book/useBookDetailsDialog'

const messagesStore = useMessagesStore()
const display = useDisplay()
const intl = useIntl()
const { convertErrorCodes } = useErrorCodeFormatter()

const { books = [], loading = false } = defineProps<{
  books?: TransientBookDto[]
  loading?: boolean
}>()

// read-write array of BookImport, will be modified by the different actions
const importBooks = ref<BookImport[]>([])

watch(
  () => books,
  (newBooks) => {
    if (newBooks) {
      importBooks.value = newBooks.map((it) => new BookImport(it))
    } else {
      importBooks.value = []
    }
  },
  {
    immediate: true,
    deep: true,
  },
)

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
    }) as BookImportBatchDto,
)

//region Table setup
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
      description: 'Import books table header: details',
      defaultMessage: 'Details',
      id: 'EbhcQd',
    }),
    key: 'details',
    align: 'center',
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

function seriesPicked(series: SeriesDto) {
  if (currentActionedItems.value) {
    currentActionedItems.value.forEach((it) => (it.series = series))
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

function bookPicked(book: BookDto) {
  if (currentActionedItems.value) {
    currentActionedItems.value.forEach((it) => (it.upgradeBook = book))
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

// auto-select importable books
watch(
  importBooks,
  (val) => {
    if (val) {
      val.forEach((it) => {
        if (it.importable && !selectedBookIds.value.includes(it.transientBook.id)) {
          selectedBookIds.value.push(it.transientBook.id)
        }
      })
    }
  },
  {
    immediate: true,
    deep: true,
  },
)

function unassignBook(book: BookImport) {
  book.upgradeBook = undefined
  book.upgradeBookPages = []
}

const { mutateAsync: postImportBooks, isLoading: importing } = useMutation({
  mutation: () => komgaImportBooks({ body: importBatch.value }),
})

function doImportBooks() {
  postImportBooks()
    .then(() => {
      selectedImportableBooks.value.forEach((it) => {
        it.imported = true
        // remove imported books from selection
        if (selectedBookIds.value.includes(it.transientBook.id))
          selectedBookIds.value.splice(selectedBookIds.value.indexOf(it.transientBook.id), 1)
      })
    })
    .catch((error) => {
      messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
    })
}

const { prepareDialog: prepareBookDetailsDialog, activator: bookDetailsActivator } =
  useBookDetailsDialog()

function bookDetails(item: BookImport) {
  prepareBookDetailsDialog(item.transientBookDetails)
}

const { prepareDialog: prepareCompareBookDialog, activator: compareBookActivator } =
  useCompareBookDialog()

function compareBooks(item: BookImport) {
  if (item.upgradeBookDetails)
    prepareCompareBookDialog(item.transientBookDetails, item.upgradeBookDetails, true)
}
</script>

<style scoped></style>
