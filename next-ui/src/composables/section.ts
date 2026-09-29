import { toValue, type MaybeRefOrGetter } from 'vue'
import { bookListQueryInfinite, booksOnDeckQueryInfinite } from '@/colada/books'
import { valuesToConditions } from '@/functions/filter'
import { ReadStatus } from '@/types/ReadStatus'
import type { SearchConditionBook, SearchConditionSeries } from '@/generated/openapi'
import { seriesListQueryInfinite, seriesUpdatedQueryInfinite } from '@/colada/series'
import type { OverviewSection } from '@/types/OverviewSection'
import type { Sort } from '@/types/PageRequest'

export function useOverviewSection(
  section: MaybeRefOrGetter<OverviewSection>,
  libraryIds: MaybeRefOrGetter<string[] | undefined>,
) {
  // compute the sort separately, can be used to determine the subtitle of item cards
  const sort = computed<Sort[]>(() => {
    switch (toValue(section)) {
      case 'on_deck':
        return []
      case 'keep_reading':
        return [{ key: 'readProgress.readDate', order: 'desc' }]
      case 'recently_released_books':
        return [{ key: 'metadata.releaseDate', order: 'desc' }]
      case 'recently_added_books':
        return [{ key: 'createdDate', order: 'desc' }]
      case 'recently_read_books':
        return [{ key: 'readProgress.readDate', order: 'desc' }]
      case 'recently_added_series':
        return [{ key: 'createdDate', order: 'desc' }]
      case 'recently_updated_series':
        return [{ key: 'lastModifiedDate', order: 'desc' }]
    }
  })

  const queryOptions = computed(() => {
    const libIds = toValue(libraryIds)

    switch (toValue(section)) {
      case 'keep_reading':
        return bookListQueryInfinite({
          search: {
            condition: {
              allOf: [
                valuesToConditions(libIds, 'libraryId'),
                { readStatus: { operator: 'is', value: ReadStatus.InProgress } },
              ].filter(Boolean) as SearchConditionBook[],
            },
          },
          sort: sort.value,
        })
      case 'on_deck':
        return booksOnDeckQueryInfinite({
          libraryIds: libIds,
        })
      case 'recently_released_books':
        return bookListQueryInfinite({
          search: {
            condition: {
              allOf: [
                valuesToConditions(libIds, 'libraryId'),
                {
                  releaseDate: {
                    operator: 'after',
                    dateTime: new Date(new Date().setMonth(new Date().getMonth() - 1)),
                  },
                },
              ].filter(Boolean) as SearchConditionBook[],
            },
          },
          sort: sort.value,
        })
      case 'recently_added_books':
        return bookListQueryInfinite({
          search: {
            condition: {
              allOf: [valuesToConditions(libIds, 'libraryId')].filter(
                Boolean,
              ) as SearchConditionBook[],
            },
          },
          sort: sort.value,
        })
      case 'recently_read_books':
        return bookListQueryInfinite({
          search: {
            condition: {
              allOf: [
                valuesToConditions(libIds, 'libraryId'),
                { readStatus: { operator: 'is', value: ReadStatus.Read } },
              ].filter(Boolean) as SearchConditionBook[],
            },
          },
          sort: sort.value,
        })
      case 'recently_added_series':
        return seriesListQueryInfinite({
          search: {
            condition: {
              allOf: [
                valuesToConditions(libIds, 'libraryId'),
                { oneshot: { operator: 'isFalse' } },
              ].filter(Boolean) as SearchConditionSeries[],
            },
          },
          sort: sort.value,
        })
      case 'recently_updated_series':
        return seriesUpdatedQueryInfinite({
          libraryIds: libIds,
        })
    }
  })

  const kind = computed(() => {
    switch (toValue(section)) {
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

  return { queryOptions, sort, kind }
}
