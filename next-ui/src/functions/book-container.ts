import { isReadList, isSeries } from '@/functions/entity'
import { PageRequest, type Sort } from '@/types/PageRequest'
import {
  type BookDto,
  type BookSearch,
  type ReadListDto,
  type SeriesDto,
} from '@/generated/openapi'
import { useQueryCache } from '@pinia/colada'
import { bookListQuery } from '@/colada/books'

/**
 * Returns the condition and page request necessary to call the book list API.
 *
 * @param parent parent series or readlist. If a string is passed, it's considered to be a series.
 * @param unreadOnly to only fetch unread books.
 */
export function getFirstBookInParentOptions(
  parent: MaybeRefOrGetter<SeriesDto | ReadListDto | string>,
  unreadOnly: boolean,
): { search: BookSearch; pageRequest: PageRequest } {
  const parentValue = toValue(parent)
  const seriesType = isSeries(parentValue) || typeof parentValue === 'string'
  const readListType = isReadList(parentValue)
  const parentId = typeof parentValue === 'string' ? parentValue : parentValue.id

  const sort: Sort[] = []
  if (seriesType) sort.push({ key: 'metadata.numberSort', order: 'asc' })
  else if (readListType) {
    if (parentValue.ordered) sort.push({ key: 'readList.number', order: 'asc' })
    else sort.push({ key: 'metadata.releaseDate', order: 'asc' })
  }

  const conditions = {
    allOf: [
      ...(seriesType
        ? [
            {
              seriesId: {
                operator: 'Is',
                value: parentId,
              },
            },
          ]
        : []),
      ...(readListType
        ? [
            {
              readListId: {
                operator: 'Is',
                value: parentId,
              },
            },
          ]
        : []),
      ...(unreadOnly
        ? [
            {
              readStatus: {
                operator: 'IsNot',
                value: 'READ',
              },
            },
          ]
        : []),
    ],
  }

  return {
    search: {
      condition: conditions,
    },
    pageRequest: new PageRequest(0, 1, sort),
  }
}

/**
 * Returns the first book in the parent.
 *
 * @param parent parent series or readlist. If a string is passed, it's considered to be a series.
 * @param unreadOnly to only fetch unread books.
 */
export async function getFirstBookInParent(
  parent: MaybeRefOrGetter<SeriesDto | ReadListDto | string>,
  unreadOnly: boolean,
): Promise<BookDto | undefined> {
  const options = getFirstBookInParentOptions(toValue(parent), unreadOnly)

  const query = bookListQuery({
    search: options.search,
    pageRequest: options.pageRequest,
  })
  const queryCache = useQueryCache()
  const cacheEntry = queryCache.ensure(query)
  const state = await queryCache.fetch(cacheEntry)

  return state.data?.content?.at(0)
}
