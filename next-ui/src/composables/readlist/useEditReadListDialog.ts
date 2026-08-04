import { storeToRefs } from 'pinia'
import { useDialogsStore } from '@/stores/dialogs'
import { useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify/framework'
import { useMessagesStore } from '@/stores/messages'

import EditMetadata from '@/components/series/form/EditMetadata.vue'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useUpdateReadList } from '@/colada/readlists'
import type { ReadListDto, ReadListUpdateDto } from '@/generated/openapi'

export function useEditReadListDialog() {
  const { confirmEdit: dialogConfirmEdit } = storeToRefs(useDialogsStore())
  const intl = useIntl()
  const display = useDisplay()
  const messagesStore = useMessagesStore()
  const { mutateAsync: mutateUpdate } = useUpdateReadList()

  const prepareDialog = (readList: ReadListDto, callback: () => void = () => {}) => {
    dialogConfirmEdit.value.dialogProps = {
      title: intl.formatMessage({
        description: 'Edit readlist dialog title',
        defaultMessage: 'Edit read list',
        id: 'bDNZqj',
      }),
      subtitle: readList.name,
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
    dialogConfirmEdit.value.record = readList
    dialogConfirmEdit.value.callback = (
      hideDialog: () => void,
      setLoading: (isLoading: boolean) => void,
    ) => {
      setLoading(true)

      const updatedData = dialogConfirmEdit.value.record as ReadListUpdateDto

      mutateUpdate({ readListId: readList.id, data: updatedData })
        .then(() => {
          hideDialog()
          messagesStore.messages.push({
            message: intl.formatMessage(
              {
                description: 'Snackbar notification shown upon successful readlist update',
                defaultMessage: 'Read list updated: {readlist}',
                id: 'IIqDdQ',
              },
              {
                readlist: updatedData.name,
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
