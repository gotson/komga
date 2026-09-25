import { useQuery } from '@pinia/colada'
import { readListDetailQuery } from '@/colada/readlists'
import type { EntityId } from '@/functions/entity'
import { type SingleBrowsingContext } from '@/functions/browsing-context'

export function useBookParentFromContext(
  context: MaybeRefOrGetter<SingleBrowsingContext | undefined>,
  enabled: MaybeRefOrGetter<boolean>,
) {
  const { data: parentReadList } = useQuery(() => ({
    ...readListDetailQuery({ readListId: toValue(context)?.id || 'none' }),
    enabled: toValue(enabled) && toValue(context)?.type === 'readList',
  }))
  const parent = computed(() => {
    if (toValue(context)?.type === 'series')
      return { kind: 'series', id: toValue(context)!.id } satisfies EntityId<'series'>
    if (toValue(context)?.type === 'readList') return parentReadList.value
  })

  return {
    parent,
  }
}
