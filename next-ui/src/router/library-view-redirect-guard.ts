import type { Router, NavigationGuardReturn } from 'vue-router'
import { useLibraries } from '@/colada/libraries'

/**
 * Redirect from /libraries/[viewId] and its child routes to the appropriate sub-route.
 * - No libraries → /libraries/create
 * - pinned/unpinned view with no data → same route with viewId 'all'
 * - Base /libraries/[viewId] → /libraries/[viewId]/overview
 */
export function useLibraryViewRedirectGuard(router: Router) {
  router.beforeEach(async (to): Promise<NavigationGuardReturn | void> => {
    if (!to.matched.some((record) => record.name === '/libraries/[viewId]')) return

    const { noLibraries, anyPinned, anyUnpinned, refresh } = useLibraries()

    await refresh()

    if (noLibraries.value) {
      return { name: '/libraries/create' }
    }

    const viewId = (to.params as { viewId?: string }).viewId

    if (!viewId) return

    const redirectToAll =
      (viewId === 'pinned' && !anyPinned.value) || (viewId === 'unpinned' && !anyUnpinned.value)

    //TODO: for now we always redirect to 'overview', this should be persisted per viewId or pinned somehow
    if (to.name === '/libraries/[viewId]') {
      return {
        name: '/libraries/[viewId]/overview',
        params: { viewId: redirectToAll ? 'all' : viewId },
      }
    }

    if (redirectToAll) {
      return {
        name: to.name,
        params: { viewId: 'all' },
      } as NavigationGuardReturn
    }
  })
}
