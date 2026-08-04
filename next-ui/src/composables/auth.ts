import { authErrorEvent } from '@/colada/error-handling'
import { logger } from '@/services/logtape'

/**
 * Watcher for the authenticated state.
 * Redirects to the login page when an authentication error happens.
 */
export function useAuthWatcher() {
  const router = useRouter()

  watch(authErrorEvent, (newValue) => {
    if (newValue) {
      logger.debug('Auth error detected, redirect to login')
      void router.push('/login')
    }
  })
}
