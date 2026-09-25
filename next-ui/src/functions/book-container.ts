import { type EntityId, isEntityId, isReadList, isSeries } from '@/functions/entity'
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
 * @param parent parent series or read list.
 * @param unreadOnly to only fetch unread books.
 */
export function getBooksInParentOptions(
  parent: MaybeRefOrGetter<SeriesDto | ReadListDto | EntityId<'series'>>,
  unreadOnly: boolean,
): { search: BookSearch; sort: Sort[] } {
  const parentValue = toValue(parent)
  const seriesType =
    isSeries(parentValue) || (isEntityId(parentValue) && parentValue.kind === 'series')
  const readListType = isReadList(parentValue)
  const parentId = parentValue.id

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
    sort: sort,
  }
}

/**
 * Returns the first book in the parent.
 *
 * @param parent parent series or read list. If a string is passed, it's considered to be a series.
 * @param unreadOnly to only fetch unread books.
 */
export async function getFirstBookInParent(
  parent: MaybeRefOrGetter<SeriesDto | ReadListDto>,
  unreadOnly: boolean,
): Promise<BookDto | undefined> {
  const options = getBooksInParentOptions(toValue(parent), unreadOnly)

  const query = bookListQuery({
    search: options.search,
    pageRequest: new PageRequest(0, 1, options.sort),
  })
  const queryCache = useQueryCache()
  const cacheEntry = queryCache.ensure(query)
  const state = await queryCache.refresh(cacheEntry)

  return state.data?.content?.at(0)
}
