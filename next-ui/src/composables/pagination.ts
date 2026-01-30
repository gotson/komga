import { syncRef } from '@vueuse/core'
import { type PageSize, SchemaStrictlyPositive } from '@/types/page'
import { useRouteQuery } from '@vueuse/router'
import * as v from 'valibot'

/**
 * Provide synchronized refs for page tracking in 0-index and 1-index.
 *
 * `page1` is initialized from `route.query.page`, the route query is kept in sync.
 *
 * The current page can never be over the `pageCount`.
 * The consumer is responsible for updating `pageCount`.
 */
export function usePagination() {
  const queryPage = useRouteQuery('page', 1, {
    transform: (input) => v.parse(SchemaStrictlyPositive, input),
  })
  const page0 = ref(queryPage.value - 1)
  const page1 = ref(queryPage.value)
  const pageCount = ref(0)

  syncRef(page0, page1, {
    transform: {
      ltr: (left) => left + 1,
      rtl: (right) => right - 1,
    },
  })
  syncRef(page1, queryPage, {
    direction: 'ltr',
  })

  watch([page0, pageCount], ([newPage0, newPageCount]) => {
    if (newPage0 > newPageCount) page0.value = newPageCount
  })

  return {
    /**
     * The 0-index current page
     */
    page0: page0,
    /**
     * The 1-index current page
     */
    page1: page1,
    /**
     * The total page count. Should be updated by the consumer.
     */
    pageCount: pageCount,
  }
}

/**
 * Reactive itemsPerPage in Vuetify format, where unpaged is converted to `-1`
 * @param pageSize
 */
export function useItemsPerPage(pageSize: MaybeRefOrGetter<PageSize>) {
  const itemsPerPage = computed(() =>
    toValue(pageSize) === 'unpaged' ? -1 : (toValue(pageSize) as number),
  )

  return {
    itemsPerPage: itemsPerPage,
  }
}
