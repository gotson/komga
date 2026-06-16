import type { components } from '@/generated/openapi/komga'
import { useQuery } from '@pinia/colada'
import { bookListQuery } from '@/colada/books'
import { PageRequest } from '@/types/PageRequest'
import { useBook } from '@/composables/book/useBook'
import { bookReaderUrl } from '@/api/links'
import { useMessagesStore } from '@/stores/messages'
import { useIntl } from 'vue-intl'

export function useSeriesBooks(seriesId: MaybeRefOrGetter<string>) {
  const messagesStore = useMessagesStore()
  const intl = useIntl()

  async function getFirstBookInSeries(
    unreadOnly: boolean,
  ): Promise<components['schemas']['BookDto'] | undefined> {
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

    const { data } = await useQuery(() =>
      bookListQuery({
        search: {
          condition: conditions as components['schemas']['AllOfBook'],
        },
        pageRequest: new PageRequest(0, 1, [{ key: 'metadata.numberSort', order: 'asc' }]),
      }),
    ).refresh()

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
          text: intl.formatMessage({
            description: 'Notification: no readable book found for series',
            defaultMessage: 'Series does not have any readable book',
            id: 'p+98v0',
          }),
        })
      }
    }
  }

  return {
    getFirstBookInSeries,
    readFirstBook,
  }
}
