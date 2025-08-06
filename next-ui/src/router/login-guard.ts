import type { Router } from 'vue-router'
import { useCurrentUser } from '@/colada/users'

// check if the user is authenticated before navigating to any page
// the authentication is cached by Pinia Colada
// redirect to the startup page if not authenticated
export function useLoginGuard(router: Router) {
  router.beforeEach((to) => {
    if (!to.meta.noAuth) {
      const { data } = useCurrentUser()
      const authenticated = data.value
      if (!authenticated) {
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
