import { useQuery } from '@pinia/colada'
import { bookListQuery } from '@/colada/books'
import { getBooksInParentOptions } from '@/functions/book-container'
import type { EntityId } from '@/functions/entity'
import { PageRequest } from '@/types/PageRequest'
import type { ReadListDto, SeriesDto } from '@/generated/openapi'

export function useBookSiblings(
  parent: MaybeRefOrGetter<SeriesDto | ReadListDto | EntityId<'series'> | undefined>,
  enabled: MaybeRefOrGetter<boolean>,
) {
  const shouldFetch = computed(() => toValue(enabled) && !!toValue(parent))

  const siblingsOptions = computed(() => {
    if (shouldFetch.value) return getBooksInParentOptions(toValue(parent)!, false)
  })
  const query = useQuery(() => ({
    ...bookListQuery({
      search: siblingsOptions.value?.search || {},
      pageRequest: PageRequest.Unpaged(siblingsOptions.value?.sort),
    }),
    enabled: shouldFetch.value && !!siblingsOptions.value,
  }))

  return {
    ...query,
  }
}
