import type { Router } from 'vue-router'
import { useClaimStatus } from '@/colada/claim'

/**
 * Check if the server has already been claimed.
 */
export function useClaimGuard(router: Router) {
  router.beforeEach(async (to) => {
    if (to.name === '/error') return

    const { refresh } = useClaimStatus()
    const { data, error } = await refresh()

    if (error) return { name: '/error' }

    const isClaimed = data?.isClaimed

    if (!isClaimed && to.name !== '/claim') {
      return { name: '/claim' }
    }

    if (isClaimed && to.name === '/claim') {
      return { name: '/' }
    }
  })
}
