import type { UseQueryEntry } from '@pinia/colada'
import { isApiErrorWithCause } from '@/api/komga-client'
import { useCurrentUser } from '@/colada/users'
import { logger } from '@/services/logtape'

export const authErrorEvent = ref(false)

export async function globalErrorHandler(error: unknown, entry: UseQueryEntry<unknown, unknown>) {
  // in case of error 401, we force a refresh of the current user to check if authentication is still valid
  if (
    isApiErrorWithCause(error) &&
    error.cause.status === 401 &&
    entry.meta?.no401handling !== true
  ) {
    logger.debug('Error 401, refetch current user')
    const { refetch, isAuthenticated } = useCurrentUser()
    await refetch()
    if (!isAuthenticated.value) {
      authErrorEvent.value = true
    }
  }
}
