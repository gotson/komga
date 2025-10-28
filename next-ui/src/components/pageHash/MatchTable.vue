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
    select-strategy="page"
    multi-sort
    mobile-breakpoint="md"
    @update:options="updateOptions"
  >
    <template #no-data>
      <EmptyStateNetworkError v-if="error" />
      <template v-else>
        {{
          $formatMessage({
            description: 'Duplicate Page Hash Matches Table: shown when table has no data',
            defaultMessage: 'No data found',
            id: 'ggaOkE',
          })
        }}
      </template>
    </template>

    <template #[`item.url`]="{ value }">
      <div
        v-if="display.xs.value"
        v-tooltip="value"
        class="text-truncate"
        style="max-width: 200px"
      >
        {{ value }}
      </div>
      <template v-else>{{ value }}</template>
    </template>

    <template #[`item.bookId`]="{ item }">
      <div>
        <v-img
          width="200"
          height="200"
          contain
          style="cursor: zoom-in"
          :src="bookPageThumbnailUrl(item.bookId, item.pageNumber)"
          lazy-src="@/assets/cover.svg"
          class="my-1"
          :alt="
            $formatMessage({
              description: 'Duplicate Page Hash Matches Table: alt description for thumbnail',
              defaultMessage: 'Duplicate page',
              id: 'V3wgcu',
            })
          "
        >
          <template #placeholder>
            <div class="d-flex align-center justify-center fill-height">
              <v-progress-circular
                color="grey"
                indeterminate
              />
            </div>
          </template>
        </v-img>
      </div>
    </template>
  </v-data-table-server>
</template>

<script setup lang="ts">
import { useIntl } from 'vue-intl'
import { PageRequest, type SortItem } from '@/types/PageRequest'
import { useQuery } from '@pinia/colada'
import { pageHashMatchesQuery } from '@/colada/page-hashes'
import { bookPageThumbnailUrl } from '@/api/images'
import { useDisplay } from 'vuetify'

const pageHash = defineModel<string>({ required: true })

const intl = useIntl()
const display = useDisplay()

//region headers
const headers = [
  {
    title: intl.formatMessage({
      description: 'Duplicate Page Hash Matches Table header: book file name',
      defaultMessage: 'Book',
      id: 'bOUy3X',
    }),
    key: 'url',
  },
  {
    title: intl.formatMessage({
      description: 'Duplicate Page Hash Matches Table header: File name',
      defaultMessage: 'File name',
      id: 'o+h0F+',
    }),
    key: 'fileName',
  },
  {
    title: intl.formatMessage({
      description: 'Duplicate Page Hash Matches Table header: page number',
      defaultMessage: 'Page number',
      id: 'm+yz6Z',
    }),
    key: 'pageNumber',
  },
  {
    title: intl.formatMessage({
      description: 'Duplicate Page Hash Matches Table header: page',
      defaultMessage: 'Page',
      id: '2Sx0J4',
    }),
    key: 'bookId',
    sortable: false,
  },
] as const
//endregion

//region Data loading
const sortBy = ref<SortItem[]>([])
const pageRequest = ref<PageRequest>(new PageRequest())

const { data, isLoading, error } = useQuery(pageHashMatchesQuery, () => ({
  pageHash: pageHash.value,
  ...pageRequest.value,
}))

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
//endregion
</script>
<script setup lang="ts"></script>
