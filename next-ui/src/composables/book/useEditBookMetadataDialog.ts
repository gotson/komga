import { storeToRefs } from 'pinia'
import { type DialogResult, useDialogsStore } from '@/stores/dialogs'
import { useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify/framework'
import { useMessagesStore } from '@/stores/messages'
import EditBook from '@/components/book/form/Edit.vue'
import PrevNext from '@/components/item/form/PrevNext.vue'
import { commonMessages } from '@/utils/i18n/common-messages'
import {
  useAddBookPoster,
  useDeleteBookPoster,
  useMarkBookPosterSelected,
  useUpdateBookMetadata,
} from '@/colada/books'
import type { BookDto } from '@/generated/openapi'
import { createEntityUpdate, type EntityUpdate } from '@/functions/poster'
import { seriesDetailQuery, useUpdateSeriesMetadata } from '@/colada/series'
import { useQuery } from '@pinia/colada'
import { type OneShotAttributes, vOneShotAttributes } from '@/types/oneshot'
import { pickSchemaKeys } from '@/functions/pick'

export function useEditBookMetadataDialog() {
  const { confirmEdit: dialogConfirmEdit } = storeToRefs(useDialogsStore())
  const intl = useIntl()
  const display = useDisplay()
  const messagesStore = useMessagesStore()
  const { mutateAsync: mutateUpdateBookMetadata } = useUpdateBookMetadata()
  const { mutateAsync: mutateUpdateSeriesMetadata } = useUpdateSeriesMetadata()
  const fetchSeriesId = ref<string | undefined>(undefined)
  const { refresh: refreshSeries } = useQuery(() => ({
    ...seriesDetailQuery({ seriesId: fetchSeriesId.value || 'none' }),
    enabled: !!fetchSeriesId.value,
  }))

  const prepareDialog = (books: BookDto[], callback: () => void = () => {}) => {
    // use a ref so we can cycle books that have been modified
    const booksToEdit = ref(books)

    // display first book
    void displayOne(booksToEdit.value.at(0)!, booksToEdit)

    dialogConfirmEdit.value.callback = async (
      result: DialogResult,
      hideDialog: () => void,
      setLoading: (isLoading: boolean) => void,
    ) => {
      if (result === 'cancel') {
        callback()
        return
      }

      setLoading(true)

      const updatedData = dialogConfirmEdit.value.record as EntityUpdate<BookDto, OneShotAttributes>
      // keep track in the ref
      booksToEdit.value = booksToEdit.value.map((it) =>
        it.id !== updatedData.entity.id ? it : updatedData.entity,
      )
      // update server
      await saveOne(updatedData)

      const bookIndex = booksToEdit.value.findIndex((it) => it.id === updatedData.entity.id)

      // if it's the last book, close dialog
      if (bookIndex === booksToEdit.value.length - 1) {
        hideDialog()
        callback()
      } else {
        // display next book
        await displayOne(booksToEdit.value.at(bookIndex + 1)!, booksToEdit)
        setLoading(false)
      }
    }
  }

  async function displayOne(book: BookDto, books: Ref<BookDto[]>) {
    const isMultiple = books.value.length > 1
    const isLast = books.value.at(-1)?.id === book.id

    dialogConfirmEdit.value.dialogProps = {
      title: intl.formatMessage({
        description: 'Edit book metadata dialog title',
        defaultMessage: 'Edit book metadata',
        id: 'mtUacw',
      }),
      subtitle: book.metadata.title,
      maxWidth: 900,
      cardTextProps: {
        class: 'px-0',
      },
      cancelText: isMultiple
        ? intl.formatMessage({
            description: 'Edit book metadata dialog: close button text',
            defaultMessage: 'Close',
            id: '1glr0U',
          })
        : undefined,
      okText: isMultiple
        ? isLast
          ? intl.formatMessage({
              description:
                'Edit multiple books metadata dialog: confirmation button text for last book',
              defaultMessage: 'Save and close',
              id: '/aKrgR',
            })
          : intl.formatMessage({
              description:
                'Edit multiple books metadata dialog: confirmation button text for all books except last',
              defaultMessage: 'Save and next',
              id: 'IqiTss',
            })
        : undefined,
      closeOnSave: false,
      scrollable: true,
      fullscreen: display.xs.value,
    }

    dialogConfirmEdit.value.slots = {
      default: {
        component: markRaw(EditBook),
      },
      prependActions: isMultiple
        ? {
            component: markRaw(PrevNext),
            props: {
              items: books,
              current: book,
            },
            handlers: {
              previous: (item: BookDto) => displayOne(item, books),
              next: (item: BookDto) => displayOne(item, books),
            },
          }
        : undefined,
    }

    dialogConfirmEdit.value.record = createEntityUpdate(book)
    // load parent series asynchronously so we don't delay the dialog opening
    if (book.oneshot) {
      fetchSeriesId.value = book.seriesId
      await refreshSeries().then(({ data }) => {
        if (data) {
          dialogConfirmEdit.value.record = createEntityUpdate(
            book,
            pickSchemaKeys(vOneShotAttributes, data.metadata),
          )
        }
      })
    }
  }

  async function saveOne(updatedData: EntityUpdate<BookDto, OneShotAttributes>) {
    // upload new posters
    if (updatedData.uploadQueue.length > 0) {
      const { mutateAsync } = useAddBookPoster()
      for (const newPoster of updatedData.uploadQueue) {
        await mutateAsync({
          bookId: updatedData.entity.id,
          file: newPoster.file,
          selected: newPoster.selected,
        }).catch((error) => {
          messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
        })
      }
    }

    // mark existing poster as selected
    if (updatedData.selected) {
      const { mutateAsync } = useMarkBookPosterSelected()
      await mutateAsync({
        bookId: updatedData.entity.id,
        thumbnailId: updatedData.selected.id,
      }).catch((error) => {
        messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
      })
    }

    // delete posters
    if (updatedData.deleteQueue.length > 0) {
      const { mutateAsync } = useDeleteBookPoster()
      for (const posterToDelete of updatedData.deleteQueue) {
        await mutateAsync({
          bookId: updatedData.entity.id,
          thumbnailId: posterToDelete.id,
        }).catch((error) => {
          messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
        })
      }
    }

    // update series if book is oneshot
    if (updatedData.entity.oneshot && updatedData.extra) {
      await mutateUpdateSeriesMetadata({
        seriesId: updatedData.entity.seriesId,
        metadata: {
          ...updatedData.extra,
          title: updatedData.entity.metadata.title,
          titleLock: updatedData.entity.metadata.titleLock,
        },
      }).catch((error) => {
        messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
      })
    }

    // update book
    const updateDto = {
      ...updatedData.entity.metadata,
      ...(updatedData.entity.oneshot ? { number: '1', numberSort: 1 } : {}),
    }
    await mutateUpdateBookMetadata({ bookId: updatedData.entity.id, metadata: updateDto })
      .then(() => {
        messagesStore.messages.push({
          message: intl.formatMessage(
            {
              description: 'Snackbar notification shown upon successful book metadata update',
              defaultMessage: 'Book metadata updated: {book}',
              id: 'P8Ox+D',
            },
            {
              book: updateDto.title,
            },
          ),
        })
      })
      .catch((error) => {
        messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
      })
  }

  const activatorRef = computed({
    get: () => dialogConfirmEdit.value.activator,
    set: (val) => (dialogConfirmEdit.value.activator = val),
  })

  function showDialog() {
    dialogConfirmEdit.value.dialogProps.shown = true
  }

  return {
    prepareDialog: prepareDialog,
    activator: activatorRef,
    showDialog: showDialog,
  }
}
