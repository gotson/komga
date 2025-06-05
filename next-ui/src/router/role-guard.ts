import type { Router } from 'vue-router'
import { useCurrentUser } from '@/colada/queries/current-user'

// check if the user has the necessary role before navigating to restricted pages
// the authentication is cached by Pinia Colada
// redirect to the home page in case of insufficient permissions
export function useRoleGuard(router: Router) {
  router.beforeEach((to) => {
    if (to.meta.requiresRole) {
      const { data } = useCurrentUser()
      if (!data.value?.roles?.includes(to.meta.requiresRole)) {
        return { name: '/' }
      }
    }
  })
}
