import { toValue, type MaybeRefOrGetter } from 'vue'
import { bookListQueryInfinite, booksOnDeckQueryInfinite } from '@/colada/books'
import { valuesToConditions } from '@/functions/filter'
import { ReadStatus } from '@/types/ReadStatus'
import type { SearchConditionBook, SearchConditionSeries } from '@/generated/openapi'
import { seriesListQueryInfinite, seriesUpdatedQueryInfinite } from '@/colada/series'
import type { OverviewSection } from '@/types/OverviewSection'

export function useOverviewSection(
  section: MaybeRefOrGetter<OverviewSection>,
  libraryIds: MaybeRefOrGetter<string[] | undefined>,
) {
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
          sort: [{ key: 'readProgress.readDate', order: 'desc' }],
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
          sort: [{ key: 'metadata.releaseDate', order: 'desc' }],
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
          sort: [{ key: 'createdDate', order: 'desc' }],
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
          sort: [{ key: 'readProgress.readDate', order: 'desc' }],
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
          sort: [{ key: 'createdDate', order: 'desc' }],
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

  return { queryOptions, kind }
}
