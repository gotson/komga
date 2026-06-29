import { useQuery } from '@pinia/colada'
import { bookListQuery } from '@/colada/books'
import { PageRequest } from '@/types/PageRequest'
import { useBook } from '@/composables/book/useBook'
import { bookReaderUrl } from '@/api/links'
import { useMessagesStore } from '@/stores/messages'
import type { AllOfBook, BookDto } from '@/generated/openapi'

export function useSeriesBooks(seriesId: MaybeRefOrGetter<string>) {
  const messagesStore = useMessagesStore()

  /**
   * Returns the Pinia Colada query to fetch the first book in the series.
   *
   * @param unreadOnly to only fetch unread books
   */
  function getFirstBookInSeriesQuery(unreadOnly: boolean) {
    const conditions = {
      allOf: [
        {
          seriesId: {
            operator: 'Is',
            value: toValue(seriesId),
          },
        },
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
          condition: conditions as AllOfBook,
        },
        pageRequest: new PageRequest(0, 1, [{ key: 'metadata.numberSort', order: 'asc' }]),
      }),
    )
  }

  /**
   * Returns the first book in the series. This is not reactive.
   *
   * @param unreadOnly to only fetch unread books
   */
  async function getFirstBookInSeries(unreadOnly: boolean): Promise<BookDto | undefined> {
    const { data } = await getFirstBookInSeriesQuery(unreadOnly).refresh()

    if (data && data.totalElements && data.totalElements > 0) {
      return data.content?.[0]
    }

    return undefined
  }

  async function readFirstBook(incognito: boolean = false) {
    let book = await getFirstBookInSeries(true)
    if (book === undefined) {
      book = await getFirstBookInSeries(false)
    }
    if (book) {
      const { canRead, isEpubReader } = useBook(book)
      if (canRead.value) {
        window.open(bookReaderUrl(book.id, isEpubReader.value, incognito), '_blank')
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
    getFirstBookInSeriesQuery,
    getFirstBookInSeries,
    readFirstBook,
  }
}
