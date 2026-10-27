import type { BookDto, SeriesDto } from '@/generated/openapi'
import { useQueryCache } from '@pinia/colada'
import { seriesDetailQuery } from '@/colada/series'

export async function getParentSeries(
  book: MaybeRefOrGetter<BookDto>,
): Promise<SeriesDto | undefined> {
  const query = seriesDetailQuery({ seriesId: toValue(book).seriesId })
  const queryCache = useQueryCache()
  const cacheEntry = queryCache.ensure(query)
  try {
    const state = await queryCache.refresh(cacheEntry)
    return state.data
  } catch (ignored) {
    return undefined
  }
}
