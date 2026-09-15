import type { Router } from 'vue-router'
import { currentUserQuery } from '@/colada/users'
import { useQueryCache } from '@pinia/colada'

/**
 * Check if the user has the necessary role before navigating to restricted pages.
 * The authentication is cached by Pinia Colada.
 * Redirect to the home page in case of insufficient permissions.
 */
export function useRoleGuard(router: Router) {
  router.beforeEach(async (to) => {
    if (to.meta.requiresRole) {
      const queryCache = useQueryCache()
      const entry = queryCache.ensure(currentUserQuery)
      const state = await queryCache.refresh(entry)

      if (!state.data?.roles?.includes(to.meta.requiresRole)) {
        return { name: '/' }
      }
    }
  })
}
