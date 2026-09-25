import { useQuery } from '@pinia/colada'
import { bookNextInSeries, bookPreviousInSeries } from '@/colada/books'
import { bookNextInReadList, bookPreviousInReadList } from '@/colada/readlists'
import { type SingleBrowsingContext } from '@/functions/browsing-context'

export function useBookNavigationFromContext(
  bookId: MaybeRefOrGetter<string>,
  context: MaybeRefOrGetter<SingleBrowsingContext | undefined>,
) {
  // in series
  const { data: previousInSeries } = useQuery(() => ({
    ...bookPreviousInSeries({ bookId: toValue(bookId) }),
    enabled: toValue(context)?.type === 'series',
  }))
  const { data: nextInSeries } = useQuery(() => ({
    ...bookNextInSeries({ bookId: toValue(bookId) }),
    enabled: toValue(context)?.type === 'series',
  }))

  // in read list
  const { data: previousInReadList } = useQuery(() => ({
    ...bookPreviousInReadList({
      readListId: toValue(context)?.type === 'readList' ? toValue(context)!.id : 'none',
      bookId: toValue(bookId),
    }),
    enabled: toValue(context)?.type === 'readList',
  }))
  const { data: nextInReadList } = useQuery(() => ({
    ...bookNextInReadList({
      readListId: toValue(context)?.type === 'readList' ? toValue(context)!.id : 'none',
      bookId: toValue(bookId),
    }),
    enabled: toValue(context)?.type === 'readList',
  }))

  const previous = computed(() => {
    if (toValue(context)?.type === 'readList') return previousInReadList.value
    if (toValue(context)?.type === 'series') return previousInSeries.value
  })
  const next = computed(() => {
    if (toValue(context)?.type === 'readList') return nextInReadList.value
    if (toValue(context)?.type === 'series') return nextInSeries.value
  })

  return {
    previous,
    next,
  }
}
