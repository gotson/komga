import { useBrowsingContext } from '@/composables/browsingContext'
import { useQuery } from '@pinia/colada'
import { bookNextInSeries, bookPreviousInSeries } from '@/colada/books'
import { bookNextInReadList, bookPreviousInReadList } from '@/colada/readlists'
import { popBrowsingContext } from '@/functions/browsing-context'

export function useBookNavigation(bookId: MaybeRefOrGetter<string>) {
  const { context } = useBrowsingContext()
  const top = computed(() => popBrowsingContext(context.value)?.top)

  // in series
  const { data: previousInSeries } = useQuery(() => ({
    ...bookPreviousInSeries({ bookId: toValue(bookId) }),
    enabled: top.value?.type === 'series',
  }))
  const { data: nextInSeries } = useQuery(() => ({
    ...bookNextInSeries({ bookId: toValue(bookId) }),
    enabled: top.value?.type === 'series',
  }))

  // in read list
  const { data: previousInReadList } = useQuery(() => ({
    ...bookPreviousInReadList({
      readListId: top.value?.type === 'readList' ? top.value?.id : 'none',
      bookId: toValue(bookId),
    }),
    enabled: top.value?.type === 'readList',
  }))
  const { data: nextInReadList } = useQuery(() => ({
    ...bookNextInReadList({
      readListId: top.value?.type === 'readList' ? top.value?.id : 'none',
      bookId: toValue(bookId),
    }),
    enabled: top.value?.type === 'readList',
  }))

  const previous = computed(() => {
    if (top.value?.type === 'readList') return previousInReadList.value
    if (top.value?.type === 'series') return previousInSeries.value
  })
  const next = computed(() => {
    if (top.value?.type === 'readList') return nextInReadList.value
    if (top.value?.type === 'series') return nextInSeries.value
  })

  return {
    previous,
    next,
    context,
    top,
  }
}
