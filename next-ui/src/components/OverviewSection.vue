<template>
  <ItemScroller
    v-if="items.length > 0"
    :items="items"
    :title="$formatMessage(overviewSectionMessages[section.section])"
    :title-to="routeTo"
    :has-next-page="hasNextPage"
    class="with-gap"
    @load-next-page="loadNextPage()"
  >
    <template #default="{ item, isSelected, toggleSelect, preSelect }">
      <div class="py-2 px-1">
        <SeriesCard
          v-if="kind === 'series'"
          :series="item as SeriesDto"
          stretch-poster
          :selected="isSelected"
          :pre-select="preSelect"
          :width="cardWidth"
          @selection="toggleSelect"
        />
        <BookCard
          v-else
          :book="item as BookDto"
          stretch-poster
          show-series
          :selected="isSelected"
          :pre-select="preSelect"
          :width="cardWidth"
          @selection="toggleSelect"
        />
      </div>
    </template>
  </ItemScroller>
</template>

<script setup lang="ts">
import { type LibraryViewId } from '@/types/libraries'
import { bookListQueryInfinite, booksOnDeckQueryInfinite } from '@/colada/books'
import { valuesToConditions } from '@/functions/filter'
import { useGetLibrariesByViewId } from '@/composables/libraries'
import { ReadStatus } from '@/types/ReadStatus'
import type {
  BookDto,
  PageBookDto,
  PageSeriesDto,
  SearchConditionBook,
  SearchConditionSeries,
  SeriesDto,
} from '@/generated/openapi'
import { seriesListQueryInfinite, seriesUpdatedQueryInfinite } from '@/colada/series'
import { useInfiniteQuery } from '@pinia/colada'
import { overviewSectionMessages } from '@/types/OverviewSection'
import { useAppStore } from '@/stores/app'
import { useDisplay } from 'vuetify'
import type { ClientSettingUserOverviewSection } from '@/types/ClientSettingsUser'
import type { RouteLocationRaw } from 'vue-router'

const props = defineProps<{
  section: ClientSettingUserOverviewSection
  libraryViewId: LibraryViewId
}>()

const display = useDisplay()
const appStore = useAppStore()
const { libraryIds } = useGetLibrariesByViewId(props.libraryViewId)

const cardWidth = computed(() => (display.smAndUp.value ? appStore.gridCardWidth : 130))

const kind = computed(() => {
  switch (props.section.section) {
    case 'keep_reading':
    case 'on_deck':
    case 'recently_released_books':
    case 'recently_added_books':
    case 'recently_read_books':
      return 'book'
    case 'recently_added_series':
    case 'recently_updated_series':
      return 'series'
  }
})

function getQueryOptions() {
  switch (props.section.section) {
    case 'keep_reading':
      return bookListQueryInfinite({
        search: {
          condition: {
            allOf: [
              valuesToConditions(libraryIds.value, 'libraryId'),
              { readStatus: { operator: 'is', value: ReadStatus.InProgress } },
            ].filter(Boolean) as SearchConditionBook[],
          },
        },
        sort: [{ key: 'readProgress.readDate', order: 'desc' }],
      })
    case 'on_deck':
      return booksOnDeckQueryInfinite({
        libraryIds: libraryIds.value,
      })
    case 'recently_released_books':
      return bookListQueryInfinite({
        search: {
          condition: {
            allOf: [
              valuesToConditions(libraryIds.value, 'libraryId'),
              {
                releaseDate: {
                  operator: 'after',
                  dateTime: new Date(new Date().setMonth(new Date().getMonth() - 1)),
                },
              },
            ].filter(Boolean) as SearchConditionBook[],
          },
        },
        sort: [{ key: 'metadata.releaseDate', order: 'desc' }],
      })
    case 'recently_added_books':
      return bookListQueryInfinite({
        search: {
          condition: {
            allOf: [valuesToConditions(libraryIds.value, 'libraryId')].filter(
              Boolean,
            ) as SearchConditionBook[],
          },
        },
        sort: [{ key: 'createdDate', order: 'desc' }],
      })
    case 'recently_read_books':
      return bookListQueryInfinite({
        search: {
          condition: {
            allOf: [
              valuesToConditions(libraryIds.value, 'libraryId'),
              { readStatus: { operator: 'is', value: ReadStatus.Read } },
            ].filter(Boolean) as SearchConditionBook[],
          },
        },
        sort: [{ key: 'readProgress.readDate', order: 'desc' }],
      })
    case 'recently_added_series':
      return seriesListQueryInfinite({
        search: {
          condition: {
            allOf: [
              valuesToConditions(libraryIds.value, 'libraryId'),
              { oneshot: { operator: 'isFalse' } },
            ].filter(Boolean) as SearchConditionSeries[],
          },
        },
        sort: [{ key: 'createdDate', order: 'desc' }],
      })
    case 'recently_updated_series':
      return seriesUpdatedQueryInfinite({
        libraryIds: libraryIds.value,
      })
  }
}

const { data, hasNextPage, loadNextPage } = useInfiniteQuery(() => getQueryOptions() as never)

const items = computed(() => {
  const pages = data.value?.pages as (PageBookDto | PageSeriesDto)[] | undefined
  return pages?.flatMap((it) => (it?.content as (BookDto | SeriesDto)[]) ?? []) ?? []
})

const routeTo = computed<RouteLocationRaw>(() => ({
  name: '/libraries/[id]/overview/[section]',
  params: { id: props.libraryViewId, section: props.section.section },
}))
</script>

<style scoped>
.with-gap :deep(.v-slide-group__content) {
  gap: 8px;
}
</style>
