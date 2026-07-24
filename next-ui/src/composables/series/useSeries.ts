import { useCurrentUser } from '@/colada/users'
import { useGetLibrariesByViewId } from '@/composables/libraries'
import type { SeriesDto } from '@/generated/openapi'

export function useSeries(series: MaybeRefOrGetter<SeriesDto>) {
  const isRead = computed(() => toValue(series).booksCount === toValue(series).booksReadCount)
  const unreadCount = computed(() =>
    toValue(series).oneshot
      ? undefined
      : toValue(series).booksUnreadCount + toValue(series).booksInProgressCount,
  )
  const inProgress = computed(
    () =>
      (!isRead.value && toValue(series).booksReadCount > 0) ||
      toValue(series).booksInProgressCount > 0,
  )

  const { hasRole } = useCurrentUser()
  const { libraries } = useGetLibrariesByViewId(() => toValue(series).libraryId)

  const isUnavailable = computed(() => toValue(series).deleted || libraries.value?.[0]?.unavailable)

  const canRead = computed(() => hasRole('PAGE_STREAMING') && !isUnavailable.value)

  return {
    isRead,
    unreadCount,
    inProgress,
    isUnavailable,
    canRead,
  }
}
