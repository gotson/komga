import type { Router, NavigationGuardReturn } from 'vue-router'
import { useQueryCache } from '@pinia/colada'
import { librariesQuery } from '@/colada/libraries'
import { clientSettingsUserQuery } from '@/colada/client-settings'
import { parseUserSettings } from '@/functions/user-settings'
import { getUserLibrariesState } from '@/functions/libraries'

/**
 * Redirect from /libraries/[viewId] and its child routes to the appropriate sub-route.
 * - No libraries → /libraries/create
 * - pinned/unpinned view with no data → same route with viewId 'all'
 * - Base /libraries/[viewId] → /libraries/[viewId]/overview
 */
export function useLibraryViewRedirectGuard(router: Router) {
  router.beforeEach(async (to): Promise<NavigationGuardReturn | void> => {
    if (!to.matched.some((record) => record.name === '/libraries/[viewId]')) return

    const viewId = (to.params as { viewId?: string }).viewId
    if (!viewId) return

    const queryCache = useQueryCache()
    const [stateLibraries, stateUserSettings] = await Promise.all([
      queryCache.refresh(queryCache.ensure(librariesQuery)),
      queryCache.refresh(queryCache.ensure(clientSettingsUserQuery)),
    ])

    const userSettings = parseUserSettings(stateUserSettings.data)
    const { noLibraries, anyPinned, anyUnpinned } = getUserLibrariesState(
      stateLibraries.data,
      userSettings,
    )

    if (noLibraries) return { name: '/libraries/create' }

    const redirectToAll =
      (viewId === 'pinned' && !anyPinned) || (viewId === 'unpinned' && !anyUnpinned)

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
