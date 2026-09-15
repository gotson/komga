import type { Router } from 'vue-router'
import { claimStatusQuery } from '@/colada/claim'
import { useQueryCache } from '@pinia/colada'

/**
 * Check if the server has already been claimed.
 */
export function useClaimGuard(router: Router) {
  router.beforeEach(async (to) => {
    if (to.name === '/claim' || to.name === '/login') {
      // check cache
      const queryCache = useQueryCache()

      const cacheEntry = queryCache.ensure(claimStatusQuery)
      const state = await queryCache.refresh(cacheEntry)

      if (state.error) return { name: '/error' }

      if (to.name === '/login' && !state.data?.isClaimed) return { name: '/claim' }
      if (to.name === '/claim' && state.data?.isClaimed) return { name: '/login' }
    }
  })
}
