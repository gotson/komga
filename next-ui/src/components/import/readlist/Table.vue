<template>
  <div>
    <v-data-table
      v-model="selectedIndices"
      :loading="creating || loading"
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
            class="ms-2"
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

      <template #[`item.request.request.series`]="{ value }">
        <div
          v-for="s in value"
          :key="s"
        >
          {{ s }}
        </div>
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
          <div
            v-else
            style="height: 2em"
            :class="isSelected(internalItem) ? 'missing' : ''"
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
          @click="finishedState || !item?.series ? undefined : (currentActionedItem = item)"
        >
          <span v-if="value">{{ value.number }} - {{ value.title }}</span>
          <div
            v-else
            style="height: 2em"
            :class="isSelected(internalItem) && item?.series ? 'missing' : ''"
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
            v-tooltip="
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
            v-tooltip="value"
            icon="i-mdi:alert-circle"
            color="error"
          />
        </template>
      </template>
    </v-data-table>

    <!--region Creation Form-->
    <v-container fluid>
      <v-row
        justify="space-between"
        align="end"
      >
        <v-col
          cols="12"
          sm=""
        >
          <v-row>
            <v-col>
              <v-text-field
                v-model="readListName"
                :rules="['required']"
                :disabled="finishedState"
                clearable
                :label="
                  $formatMessage({
                    description: 'Import reading list: bottom bar: reading list name',
                    defaultMessage: 'Name',
                    id: 'rrF/Z2',
                  })
                "
                :error-messages="readListNameAlreadyExists ? duplicateNameMessage : undefined"
              ></v-text-field>
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
        </v-col>

        <v-col cols="auto">
          <v-btn
            :color="finishedState ? 'success' : 'primary'"
            :text="
              $formatMessage({
                description: 'Import reading list: bottom bar: create button',
                defaultMessage: 'Create',
                id: 'dipMGb',
              })
            "
            :disabled="!isFormValid || creating || loading || finishedState"
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
import type { components } from '@/generated/openapi/komga'
import { useIntl } from 'vue-intl'
import {
  asyncComputed,
  syncRefs,
  useArrayFilter,
  useArrayMap,
  useMemoize,
  watchImmediate,
} from '@vueuse/core'
import { useDisplay } from 'vuetify'
import { useQuery } from '@pinia/colada'
import { bookListQuery } from '@/colada/books'
import { useCreateReadList, useListReadLists } from '@/colada/readlists'
import { useMessagesStore } from '@/stores/messages'
import type { ErrorCause } from '@/api/komga-client'
import { commonMessages } from '@/utils/i18n/common-messages'
import { PageRequest } from '@/types/PageRequest'

class ReadListEntry {
  index: number
  request: components['schemas']['ReadListRequestBookMatchesDto']
  series?: components['schemas']['ReadListRequestBookMatchSeriesDto']
  book?: components['schemas']['ReadListRequestBookMatchBookDto']

  constructor(request: components['schemas']['ReadListRequestBookMatchesDto'], index: number) {
    this.index = index
    this.request = request
    const match = request.matches.find(Boolean)
    if (match) {
      this.series = match.series
      this.book = match.books.find(Boolean)
    }
  }

  public get selectable(): boolean {
    return !finishedState.value
  }

  public get importable(): boolean {
    return !!this.book
  }

  public get statusMessage(): string {
    if (!this.series)
      return intl.formatMessage({
        description: 'Import reading list: status message: choose a series',
        defaultMessage: 'Choose a series',
        id: 'H2B6uF',
      })
    if (!this.book)
      return intl.formatMessage({
        description: 'Import reading list: status message: choose a book',
        defaultMessage: 'Choose a book',
        id: 'xYp/8u',
      })
    return ''
  }
}

const display = useDisplay()
const intl = useIntl()
const messagesStore = useMessagesStore()

const { match, loading = false } = defineProps<{
  match: components['schemas']['ReadListRequestMatchDto']
  loading?: boolean
}>()

// a read-only array of ReadListEntry, kept in sync with the requests prop
const readListEntriesRO = useArrayMap(
  toRef(() => match.requests),
  (it, index) => new ReadListEntry(it, index + 1),
)
// read-write array of ReadListEntry, will be modified by the different actions
const readListEntries = ref<ReadListEntry[]>([])
// 1-way sync from readListEntriesRO to readListEntries
syncRefs(readListEntriesRO, readListEntries)

// the current item being acted upon, used for dialog callback
const currentActionedItem = ref<ReadListEntry>()
// the selected indices, used to programmatically select items
const selectedIndices = ref<number[]>([])
// a read-only list of the request indices, used to select all on initialization
const entriesIndex = useArrayMap(readListEntriesRO, (it) => it.index)
// 1-way sync from entriesIndex to selectedIndices, will effectively select all on initialization
syncRefs(entriesIndex, selectedIndices)

// the selected books
const selectedBooks = useArrayFilter(readListEntries, (it) =>
  selectedIndices.value.includes(it.index),
)

const duplicateBookIds = useArrayFilter(
  useArrayMap(selectedBooks, (it) => it.book?.bookId),
  (it, index, array) => !!it && array.indexOf(it) !== index,
)

const readListName = ref<string>(match.readListMatch.name)
const readListSummary = ref<string>()
const readListCreated = ref<components['schemas']['ReadListDto']>()
// if the prop changes, reset some data
watchImmediate(
  () => match,
  (m) => {
    readListName.value = m.readListMatch.name
    readListSummary.value = ''
    readListCreated.value = undefined
  },
)

//region Duplicate read list name check
const { data: allReadLists } = useQuery(useListReadLists({ pageRequest: PageRequest.Unpaged() }))
const readListNameAlreadyExists = computed(() =>
  allReadLists.value?.content?.some(
    (it) => it.name.localeCompare(readListName.value, undefined, { sensitivity: 'accent' }) == 0,
  ),
)
const duplicateNameMessage = intl.formatMessage({
  description: 'Import reading list: error message if read list name already exists',
  defaultMessage: 'A read list with that name already exists',
  id: 'LjqS9+',
})
//endregion

const finishedState = computed<boolean>(() => !!readListCreated.value)

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
    title: 'OK',
    value: 'o',
  },
  {
    title: 'Error',
    value: 'e',
  },
  {
    title: 'Duplicate',
    value: 'd',
  },
]

function filterFn(
  value: string,
  query: string,
  item?: { raw: ReadListEntry },
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

async function seriesPicked(series: components['schemas']['SeriesDto']) {
  if (currentActionedItem.value) {
    currentActionedItem.value.series = {
      seriesId: series.id,
      title: series.metadata.title,
      releaseDate: series.booksMetadata?.releaseDate,
    }

    const requestedNumber = currentActionedItem.value.request.request.number
    const seriesBooks = await getSeriesBooks(series.id)
    if (seriesBooks) {
      const matchedBook = seriesBooks.content?.find((b) => b.metadata.number === requestedNumber)
      if (matchedBook) bookPicked(matchedBook)
    }
  }
}
//endregion

//region Book Picker Dialog
const dialogBookPickerActivator = ref<Element | undefined>(undefined)
const dialogBookPickerBooks = asyncComputed(async () =>
  currentActionedItem.value?.series
    ? (await getSeriesBooks(currentActionedItem.value.series.seriesId))?.content
    : undefined,
)

function bookPicked(book: components['schemas']['BookDto']) {
  if (currentActionedItem.value) {
    currentActionedItem.value.book = {
      bookId: book.id,
      title: book.metadata.title,
      number: book.metadata.number,
    }
  }
}
//endregion

const getSeriesBooks = useMemoize(async (seriesId: string) =>
  useQuery(bookListQuery, () => ({
    search: {
      condition: {
        seriesId: { operator: 'Is', value: seriesId },
      },
    } as components['schemas']['BookSearch'],
  }))
    .refresh()
    .then(({ data }) => data),
)

const isFormValid = computed<boolean>(
  () => !!readListName.value && !readListNameAlreadyExists.value,
)
const createPayload = computed(
  () =>
    ({
      name: readListName.value,
      summary: readListSummary.value,
      ordered: true,
      bookIds: selectedBooks.value.map((it) => it.book?.bookId),
    }) as components['schemas']['ReadListCreationDto'],
)

const { mutateAsync: postReadList, isLoading: creating } = useCreateReadList()

function doCreateReadList() {
  if (selectedBooks.value.length == 0) {
    messagesStore.messages.push('Select some books')
    return
  }
  if (selectedBooks.value.some((it) => !it.importable)) {
    messagesStore.messages.push('Some of the selected books are in error')
    return
  }
  if (selectedBooks.value.some((it) => duplicateBookIds.value?.includes(it.book?.bookId))) {
    messagesStore.messages.push('Some of the selected books are duplicates')
    return
  }

  postReadList(createPayload.value)
    .then(({ data }) => {
      readListCreated.value = data
      //TODO: add link to created readlist
      messagesStore.messages.push({
        text: 'Readlist created',
      })
    })
    .catch((error) => {
      messagesStore.messages.push({
        text:
          (error?.cause as ErrorCause)?.message || intl.formatMessage(commonMessages.networkError),
      })
    })
}
</script>

<style scoped>
.missing {
  border: 2px dashed red;
}
</style>
