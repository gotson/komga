import type { Router } from 'vue-router'
import { useClaimStatus } from '@/colada/claim'

/**
 * Check if the server has already been claimed.
 */
export function useClaimGuard(router: Router) {
  router.beforeEach(async (to) => {
    if (to.name === '/claim' || to.name === '/login') {
      const { data, error } = await useClaimStatus().refresh()

      if (error) return { name: '/error' }

      if (to.name === '/login' && !data?.isClaimed) return { name: '/claim' }
      if (to.name === '/claim' && data?.isClaimed) return { name: '/login' }
    }
  })
}
