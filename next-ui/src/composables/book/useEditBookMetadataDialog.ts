import { storeToRefs } from 'pinia'
import { type DialogResult, useDialogsStore } from '@/stores/dialogs'
import { useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify/framework'
import { useMessagesStore } from '@/stores/messages'
import EditBook from '@/components/book/form/Edit.vue'
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

  const prepareDialog = (book: BookDto, callback: () => void = () => {}) => {
    const { refresh: refreshSeries } = useQuery(() => ({
      ...seriesDetailQuery({ seriesId: book.seriesId }),
      enabled: book.oneshot,
    }))

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
      closeOnSave: false,
      scrollable: true,
      fullscreen: display.xs.value,
    }
    dialogConfirmEdit.value.slot = {
      component: markRaw(EditBook),
    }
    dialogConfirmEdit.value.record = createEntityUpdate(book)

    // load parent series asynchronously so we don't delay the dialog opening
    if (book.oneshot) {
      void refreshSeries().then(({ data }) => {
        if (data) {
          dialogConfirmEdit.value.record = createEntityUpdate(
            book,
            pickSchemaKeys(vOneShotAttributes, data.metadata),
          )
        }
      })
    }

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
      if (book.oneshot && updatedData.extra) {
        await mutateUpdateSeriesMetadata({
          seriesId: book.seriesId,
          metadata: updatedData.extra,
        }).catch((error) => {
          messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
        })
      }

      // update book
      const updateDto = updatedData.entity.metadata
      mutateUpdateBookMetadata({ bookId: book.id, metadata: updateDto })
        .then(() => {
          hideDialog()
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
          setLoading(false)
        })
        .finally(() => callback())
    }
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
