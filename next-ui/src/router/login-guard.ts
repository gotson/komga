import type { Router } from 'vue-router'
import { currentUserQuery } from '@/colada/users'
import { useQueryCache } from '@pinia/colada'

/**
 * Check if the user is authenticated before navigating to any page.
 * The authentication is cached by Pinia Colada.
 * Redirect to the startup page if not authenticated.
 */
export function useLoginGuard(router: Router) {
  router.beforeEach(async (to) => {
    if (!to.meta.noAuth) {
      const queryCache = useQueryCache()
      const entry = queryCache.ensure(currentUserQuery)
      const state = await queryCache.refresh(entry)

      const isAuthenticated = !!state.data && !state.error

      if (!isAuthenticated) {
        const query = Object.assign(
          {},
          to.query,
          to.fullPath !== '/' ? { redirect: to.fullPath } : {},
        )
        return { name: '/startup', query: query }
      }
    }
  })
}
