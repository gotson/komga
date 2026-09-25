import type { Router } from 'vue-router'
import { useQueryCache } from '@pinia/colada'
import { logger } from '@/services/logtape'
import { seriesDetailQuery } from '@/colada/series'
import { getFirstBookInParent } from '@/functions/book-container'

/**
 * Check if the series is a oneshot, and redirect to the book page if needed.
 */
export function useOneshotGuard(router: Router) {
  router.beforeEach(async (to) => {
    if (to.name === '/series/[id]') {
      logger.debug('navigation guard: check if series is oneshot')

      // check cache
      const queryCache = useQueryCache()
      const cacheEntry = queryCache.ensure(seriesDetailQuery({ seriesId: to.params.id }))
      const state = await queryCache.refresh(cacheEntry)
      const series = state.data

      if (series?.oneshot) {
        logger.debug('navigation guard: series is oneshot, fetch book for redirection')
        const book = await getFirstBookInParent(series, false)

        if (book) {
          logger.debug('navigation guard: book found, redirect to book page')
          return {
            name: '/book/[id]',
            params: { id: book.id },
            query: to.query,
          }
        }
      }
    }
  })
}
