import { storeToRefs } from 'pinia'
import { useDialogsStore } from '@/stores/dialogs'
import { useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify/framework'
import { useMessagesStore } from '@/stores/messages'
import type { components } from '@/generated/openapi/komga'
import EditMetadata from '@/components/series/form/EditMetadata.vue'
import type { ErrorCause } from '@/api/komga-client'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useUpdateBookMetadata } from '@/colada/books'

export function useEditBookMetadataDialog() {
  const { confirmEdit: dialogConfirmEdit } = storeToRefs(useDialogsStore())
  const intl = useIntl()
  const display = useDisplay()
  const messagesStore = useMessagesStore()
  const { mutateAsync: mutateUpdateSeriesMetadata } = useUpdateBookMetadata()

  const prepareDialog = (book: components['schemas']['BookDto']) => {
    dialogConfirmEdit.value.dialogProps = {
      title: intl.formatMessage({
        description: 'Edit book metadata dialog title',
        defaultMessage: 'Edit book metadata',
        id: 'mtUacw',
      }),
      subtitle: book.metadata.title,
      maxWidth: 600,
      okText: 'Save',
      cardTextClass: 'px-0',
      closeOnSave: false,
      scrollable: true,
      fullscreen: display.xs.value,
    }
    dialogConfirmEdit.value.slot = {
      component: markRaw(EditMetadata),
    }
    dialogConfirmEdit.value.record = book.metadata
    dialogConfirmEdit.value.callback = (
      hideDialog: () => void,
      setLoading: (isLoading: boolean) => void,
    ) => {
      setLoading(true)

      const updatedMetadata = dialogConfirmEdit.value
        .record as components['schemas']['BookMetadataDto']

      mutateUpdateSeriesMetadata({ bookId: book.id, metadata: updatedMetadata })
        .then(() => {
          hideDialog()
          messagesStore.messages.push({
            text: intl.formatMessage(
              {
                description: 'Snackbar notification shown upon successful book metadata update',
                defaultMessage: 'Book metadata updated: {book}',
                id: 'P8Ox+D',
              },
              {
                book: updatedMetadata.title,
              },
            ),
          })
        })
        .catch((error) => {
          messagesStore.messages.push({
            text:
              (error?.cause as ErrorCause)?.message ||
              intl.formatMessage(commonMessages.networkError),
          })
          setLoading(false)
        })
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
