import { useQuery } from '@pinia/colada'
import { bookListQuery } from '@/colada/books'
import { PageRequest, type Sort } from '@/types/PageRequest'
import { useBook } from '@/composables/book/useBook'
import { bookReaderUrl } from '@/api/links'
import { useMessagesStore } from '@/stores/messages'
import type { BookDto, ReadListDto, SeriesDto } from '@/generated/openapi'
import { isReadList, isSeries } from '@/functions/entity'

/**
 * Provide functions to retrieve books from either a series or a read list
 * @param parent a SeriesDto, ReadListDto, or seriesId as string
 */
export function useBooks(parent: MaybeRefOrGetter<SeriesDto | ReadListDto | string>) {
  const messagesStore = useMessagesStore()

  /**
   * Returns the Pinia Colada query to fetch the first book in the parent (series or read list).
   *
   * @param unreadOnly to only fetch unread books
   */
  function getFirstBookInParentQuery(unreadOnly: boolean) {
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

    return useQuery(() =>
      bookListQuery({
        search: {
          condition: conditions,
        },
        pageRequest: new PageRequest(0, 1, sort),
      }),
    )
  }

  /**
   * Returns the first book in the series. This is not reactive.
   *
   * @param unreadOnly to only fetch unread books
   */
  async function getFirstBookInParent(unreadOnly: boolean): Promise<BookDto | undefined> {
    const { data } = await getFirstBookInParentQuery(unreadOnly).refresh()

    if (data && data.totalElements && data.totalElements > 0) {
      return data.content?.[0]
    }

    return undefined
  }

  async function readFirstBook(incognito: boolean = false) {
    let book = await getFirstBookInParent(true)
    if (book === undefined) {
      book = await getFirstBookInParent(false)
    }
    if (book) {
      const { canRead, isEpubReader } = useBook(book)
      if (canRead.value) {
        const parentValue = toValue(parent)
        window.open(
          bookReaderUrl(
            book.id,
            isEpubReader.value,
            incognito,
            isReadList(parentValue) ? parentValue.id : undefined,
          ),
          '_blank',
        )
      } else {
        messagesStore.messages.push({
          message: {
            description: 'Notification: no readable book found for series',
            defaultMessage: 'Series does not have any readable book',
            id: 'p+98v0',
          },
        })
      }
    }
  }

  return {
    getFirstBookInParentQuery,
    getFirstBookInParent,
    readFirstBook,
  }
}
