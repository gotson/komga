<template>
  <div>
    <v-data-table
      v-model="selectedIndices"
      :loading="isLoading"
      :items="readListEntries"
      item-value="index"
      :headers="headers"
      :search="filterRef"
      :custom-filter="filterFn"
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
          icon="i-mdi:filter"
          :title="
            $formatMessage({
              description:
                'Import reading list table: shown when table has no data because of the selected filter - title',
              defaultMessage: 'No data',
              id: 'AJa6Tq',
            })
          "
          :text="
            $formatMessage({
              description:
                'Import reading list table: shown when table has no data because of the selected filter - subtitle',
              defaultMessage: 'Try adjusting the filters',
              id: 'NpjqFA',
            })
          "
        />
      </template>

      <template #top>
        <v-toolbar flat>
          <v-spacer />
          <v-chip-group
            v-model="filterSelect"
            multiple
            class="mx-2"
            :disabled="finishedState"
          >
            <v-chip
              v-for="f in filterOptions"
              :key="f.value"
              :value="f.value"
              :text="f.title"
              filter
              rounded
              color="primary"
            />
          </v-chip-group>
        </v-toolbar>
      </template>

      <template #[`item.request.request.series`]="{ value, item }">
        <div
          v-for="s in value"
          :key="s"
        >
          {{ s }}
        </div>
        <ImportReadlistEntryWatcher
          :entry="item"
          @update:series-books="(books) => (item.seriesBooks = books)"
          @update:matched-book="(book) => (item.book = book)"
        />
      </template>

      <template #[`item.series`]="{ item, internalItem, isSelected, value }">
        <div
          :class="finishedState ? undefined : 'cursor-pointer'"
          @mouseenter="
            finishedState
              ? undefined
              : (dialogSeriesPickerActivator = $event.currentTarget as Element)
          "
          @click="finishedState ? undefined : (currentActionedItem = item)"
        >
          <template v-if="value">
            <div>{{ value?.title }}</div>
            <div v-if="value?.releaseDate">
              {{ $formatDate(value?.releaseDate, { year: 'numeric', timeZone: 'UTC' }) }}
            </div>
          </template>
          <v-btn
            v-else
            :text="
              $formatMessage({
                description: 'Import reading list table: series cell: select series button',
                defaultMessage: 'Select series',
                id: 'gojrIJ',
              })
            "
            :disabled="!item.selectable"
            :color="isSelected(internalItem) ? 'error' : ''"
            size="small"
          />
        </div>
      </template>

      <template #[`item.book`]="{ item, internalItem, isSelected, value }">
        <div
          :class="finishedState || !item?.series ? undefined : 'cursor-pointer'"
          @mouseenter="
            finishedState || !item?.series
              ? undefined
              : (dialogBookPickerActivator = $event.currentTarget as Element)
          "
          @click="finishedState || !item?.series ? undefined : prepareBookPicker(item)"
        >
          <span v-if="value">{{ value.number }} - {{ value.title }}</span>
          <v-btn
            v-else
            :text="
              $formatMessage({
                description: 'Import reading list table: book cell: select book button',
                defaultMessage: 'Select book',
                id: 'noaGSP',
              })
            "
            :disabled="!item.selectable || !item?.series"
            :color="isSelected(internalItem) && item?.series ? 'error' : ''"
            size="small"
          />
        </div>
      </template>

      <template #[`header.statusMessage`]>
        <v-icon icon="i-mdi:alert-circle-outline" />
      </template>

      <template #[`item.statusMessage`]="{ item, value, internalItem, isSelected }">
        <template v-if="isSelected(internalItem)">
          <v-icon
            v-if="duplicateBookIds?.includes(item.book?.bookId)"
            v-ktooltip="
              $formatMessage({
                description: 'Import reading list table: tooltip for status - duplicate book',
                defaultMessage: 'Duplicate book',
                id: '1MAL38',
              })
            "
            icon="i-mdi:alert-circle"
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

    <!--region Creation Form-->
    <v-container fluid>
      <v-row>
        <v-col>
          <v-text-field
            v-model="readListName"
            :rules="[rules.required()]"
            :disabled="finishedState"
            clearable
            :label="
              $formatMessage({
                description: 'Import reading list: bottom bar: reading list name',
                defaultMessage: 'Name',
                id: 'rrF/Z2',
              })
            "
            :error-messages="readListNameErrorMessage"
          />
        </v-col>
      </v-row>

      <v-row>
        <v-col>
          <v-textarea
            v-model="readListSummary"
            rows="2"
            hide-details
            :disabled="finishedState"
            clearable
            :label="
              $formatMessage({
                description: 'Import reading list: bottom bar: reading list summary',
                defaultMessage: 'Summary',
                id: 'uW+6XG',
              })
            "
          />
        </v-col>
      </v-row>

      <v-row class="align-center justify-end">
        <v-col cols="auto">
          <v-checkbox
            v-model="readListOverwrite"
            :disabled="readListDuplicate === undefined"
            hide-details
            :label="
              $formatMessage({
                description:
                  'Import reading list: bottom bar: reading list overwrite checkbox label',
                defaultMessage: 'Overwrite existing read list',
                id: 'PguYSD',
              })
            "
          />
        </v-col>
        <v-col cols="auto">
          <v-btn
            min-width="120px"
            :color="finishedState ? 'success' : 'primary'"
            :text="
              shouldOverwrite
                ? $formatMessage({
                    description: 'Import reading list: bottom bar: overwrite button',
                    defaultMessage: 'Overwrite',
                    id: 'BtDfUf',
                  })
                : $formatMessage({
                    description: 'Import reading list: bottom bar: create button',
                    defaultMessage: 'Create',
                    id: 'dipMGb',
                  })
            "
            :disabled="!isFormValid || isLoading || finishedState"
            :prepend-icon="finishedState ? 'i-mdi:check' : undefined"
            @click="doCreateReadList"
          />
        </v-col>
      </v-row>
    </v-container>
    <!--endregion-->

    <DialogSeriesPicker
      :activator="dialogSeriesPickerActivator"
      :fullscreen="display.xs.value"
      @selected-series="(series) => seriesPicked(series)"
    />

    <DialogBookPicker
      :activator="dialogBookPickerActivator"
      :fullscreen="display.xs.value"
      :books="dialogBookPickerBooks"
      @selected-book="(book) => bookPicked(book)"
    />
  </div>
</template>

<script setup lang="ts">
import { defineMessage, useIntl } from 'vue-intl'
import { useArrayFilter, useArrayMap, watchImmediate } from '@vueuse/core'
import { useDisplay, useRules } from 'vuetify'
import { useQuery } from '@pinia/colada'
import { readListsListQuery, useCreateReadList, useUpdateReadList } from '@/colada/readlists'
import { useMessagesStore } from '@/stores/messages'
import { commonMessages } from '@/utils/i18n/common-messages'
import { PageRequest } from '@/types/PageRequest'
import type {
  BookDto,
  ReadListCreationDto,
  ReadListRequestMatchDto,
  SeriesDto,
} from '@/generated/openapi'
import { ReadListImportEntry } from '@/types/ReadListImportEntry'

const display = useDisplay()
const intl = useIntl()
const rules = useRules()
const messagesStore = useMessagesStore()

const { match, loading = false } = defineProps<{
  match: ReadListRequestMatchDto
  loading?: boolean
}>()

// read list entries: will be modified by the different actions
const readListEntries = ref<ReadListImportEntry[]>([])

// form
const readListName = ref<string>(match.readListMatch.name)
const readListOverwrite = ref<boolean>(false)
const readListSummary = ref<string>()

// created read list
const readListCreatedId = ref<string>()
const finishedState = computed<boolean>(() => !!readListCreatedId.value)

// the current item being acted upon, used for dialog callback
const currentActionedItem = ref<ReadListImportEntry>()
// the selected indices, used to programmatically select items
const selectedIndices = ref<number[]>([])

// the selected books
const selectedBooks = useArrayFilter(readListEntries, (it) =>
  selectedIndices.value.includes(it.index),
)

const duplicateBookIds = useArrayFilter(
  useArrayMap(selectedBooks, (it) => it.book?.bookId),
  (it, index, array) => !!it && array.indexOf(it) !== index,
)

watchImmediate(
  () => match,
  (m) => {
    // reinitialize form
    readListName.value = m.readListMatch.name
    readListSummary.value = ''

    // clear created state
    readListCreatedId.value = undefined

    // create entries
    readListEntries.value = m.requests.map(
      (it, index) => new ReadListImportEntry(it, index + 1, () => !finishedState.value),
    )

    // select all
    selectedIndices.value = Array.from({ length: m.requests.length }, (_, i) => i + 1)
  },
)

//region Duplicate read list name check
const { data: allReadLists } = useQuery(readListsListQuery({ pageRequest: PageRequest.Unpaged() }))
const readListDuplicate = computed(() =>
  allReadLists.value?.content?.find(
    (it) => it.name.localeCompare(readListName.value, undefined, { sensitivity: 'accent' }) == 0,
  ),
)
const duplicateNameMessage = intl.formatMessage({
  description: 'Import reading list: error message if read list name already exists',
  defaultMessage: 'A read list with that name already exists',
  id: 'LjqS9+',
})
const readListNameErrorMessage = computed(() => {
  if (readListDuplicate.value && !readListOverwrite.value) return duplicateNameMessage
  return undefined
})
const shouldOverwrite = computed(() => !!readListDuplicate.value && readListOverwrite.value)
//endregion

//region Table setup
const hideFooter = computed(() => readListEntries.value.length < 10)

const headers = [
  {
    title: '#',
    key: 'index',
  },
  {
    title: intl.formatMessage({
      description: 'Import reading list table header: requested series',
      defaultMessage: 'Requested series',
      id: 'LD5j8J',
    }),
    key: 'request.request.series',
  },
  {
    title: intl.formatMessage({
      description: 'Import reading list table header: requested number',
      defaultMessage: 'Requested number',
      id: 'Wlzzv8',
    }),
    key: 'request.request.number',
  },
  {
    title: intl.formatMessage({
      description: 'Import reading list table header: Series',
      defaultMessage: 'Series',
      id: 'ThHjN4',
    }),
    key: 'series',
  },
  {
    title: intl.formatMessage({
      description: 'Import reading list table header: Book',
      defaultMessage: 'Book',
      id: '700A3r',
    }),
    key: 'book',
  },
  {
    title: intl.formatMessage({
      description: 'Import reading list table header: status message',
      defaultMessage: 'Status',
      id: 'J44THG',
    }),
    key: 'statusMessage',
    align: 'end',
  },
] as const // workaround for https://github.com/vuetifyjs/vuetify/issues/18901
//endregion

//region Filtering
const filterSelect = ref<string[]>([])
const filterRef = computed(() => filterSelect.value.join(''))
const filterOptions = [
  {
    title: intl.formatMessage({
      description: 'Import reading list table filter: OK',
      defaultMessage: 'OK',
      id: '0ILHru',
    }),
    value: 'o',
  },
  {
    title: intl.formatMessage({
      description: 'Import reading list table filter: Error',
      defaultMessage: 'Error',
      id: 'toP54Z',
    }),
    value: 'e',
  },
  {
    title: intl.formatMessage({
      description: 'Import reading list table filter: duplicate',
      defaultMessage: 'Duplicate',
      id: 'ALGXT9',
    }),
    value: 'd',
  },
]

function filterFn(
  value: string,
  query: string,
  item?: { raw: ReadListImportEntry },
): boolean | number | [number, number] | [number, number][] {
  const error = item?.raw.statusMessage
  const duplicate = duplicateBookIds.value.includes(item?.raw.book?.bookId)
  if (error && query.includes('e')) return true
  if (!error && duplicate && query.includes('d')) return true
  if (!error && !duplicate && query.includes('o')) return true
  return false
}
//endregion

//region Series Picker Dialog
const dialogSeriesPickerActivator = ref<Element | undefined>(undefined)

function seriesPicked(series: SeriesDto) {
  if (currentActionedItem.value) {
    currentActionedItem.value.series = {
      seriesId: series.id,
      title: series.metadata.title,
      releaseDate: series.booksMetadata?.releaseDate,
    }
    currentActionedItem.value.shouldFetchSeriesBooks = true
  }
}
//endregion

//region Book Picker Dialog
const dialogBookPickerActivator = ref<Element | undefined>(undefined)
const dialogBookPickerBooks = computed(() =>
  currentActionedItem.value?.series ? currentActionedItem.value.seriesBooks : undefined,
)

function prepareBookPicker(item: ReadListImportEntry) {
  item.shouldFetchSeriesBooks = true
  currentActionedItem.value = item
}

function bookPicked(book: BookDto) {
  if (currentActionedItem.value) {
    currentActionedItem.value.book = {
      bookId: book.id,
      title: book.metadata.title,
      number: book.metadata.number,
    }
  }
}
//endregion

const isFormValid = computed<boolean>(() => !!readListName.value && !readListNameErrorMessage.value)
const createPayload = computed(
  () =>
    ({
      name: readListName.value,
      summary: readListSummary.value,
      ordered: true,
      bookIds: selectedBooks.value.map((it) => it.book?.bookId),
    }) as ReadListCreationDto,
)

const { mutateAsync: postReadList, isLoading: creating } = useCreateReadList()
const { mutateAsync: updateReadList, isLoading: updating } = useUpdateReadList()
const isLoading = computed(() => creating.value || updating.value || loading)

function doCreateReadList() {
  if (selectedBooks.value.length == 0) {
    messagesStore.messages.push(
      defineMessage({
        description:
          'Create read list error: user need to select at least 1 book before creating a read list',
        defaultMessage: 'Select some books',
        id: 'aYalqk',
      }),
    )
    return
  }
  if (selectedBooks.value.some((it) => !it.importable)) {
    messagesStore.messages.push(
      defineMessage({
        description: 'Create read list error: some of the selected books are in error',
        defaultMessage: 'Some of the selected books are in error',
        id: 'oQ2wIE',
      }),
    )
    return
  }
  if (selectedBooks.value.some((it) => duplicateBookIds.value?.includes(it.book?.bookId))) {
    messagesStore.messages.push(
      defineMessage({
        description: 'Create read list error: some of the selected books are duplicates',
        defaultMessage: 'Some of the selected books are duplicates',
        id: '/YxHru',
      }),
    )
    return
  }

  if (shouldOverwrite.value) {
    updateReadList({ readListId: readListDuplicate.value!.id, data: createPayload.value })
      .then(() => success(readListDuplicate.value!.id))
      .catch((error) =>
        messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError),
      )
  } else {
    postReadList(createPayload.value)
      .then((data) => {
        if (data) success(data.id)
      })
      .catch((error) =>
        messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError),
      )
  }
}

function success(readListId: string) {
  readListCreatedId.value = readListId
  messagesStore.messages.push({
    message: commonMessages.readListCreated,
    action: {
      to: { name: '/readlist/[id]', params: { id: readListId } },
      label: commonMessages.notificationOpen,
    },
  })
}
</script>
