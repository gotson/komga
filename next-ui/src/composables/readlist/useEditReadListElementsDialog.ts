import { storeToRefs } from 'pinia'
import { type DialogResult, useDialogsStore } from '@/stores/dialogs'
import { useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify/framework'
import { useMessagesStore } from '@/stores/messages'

import ReadListOrganize from '@/components/readlist/Organize.vue'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useUpdateReadList } from '@/colada/readlists'
import type { ReadListDto } from '@/generated/openapi'

import { pick } from '@/functions/object'

export function useEditReadListElementsDialog() {
  const { confirmEdit: dialogConfirmEdit } = storeToRefs(useDialogsStore())
  const intl = useIntl()
  const display = useDisplay()
  const messagesStore = useMessagesStore()
  const { mutateAsync: mutateUpdate } = useUpdateReadList()

  const prepareDialog = (readList: ReadListDto, callback: () => void = () => {}) => {
    dialogConfirmEdit.value.dialogProps = {
      title: intl.formatMessage({
        description: 'Edit readlist elements dialog title',
        defaultMessage: 'Organize read list',
        id: 'r48oBl',
      }),
      subtitle: readList.name,
      maxWidth: 900,
      cardTextProps: {
        class: 'px-0',
      },
      closeOnSave: false,
      scrollable: true,
      fullscreen: display.xs.value,
    }
    dialogConfirmEdit.value.slots = {
      default: {
        component: markRaw(ReadListOrganize),
      },
    }
    dialogConfirmEdit.value.record = readList
    dialogConfirmEdit.value.callback = (
      result: DialogResult,
      hideDialog: () => void,
      setLoading: (isLoading: boolean) => void,
    ) => {
      if (result === 'cancel') {
        callback()
        return
      }

      setLoading(true)

      const updatedData = dialogConfirmEdit.value.record as ReadListDto

      // update read list
      const updateDto = pick(updatedData, 'bookIds')
      mutateUpdate({ readListId: readList.id, data: updateDto })
        .then(() => {
          hideDialog()
          messagesStore.messages.push({
            message: intl.formatMessage(
              {
                description: 'Snackbar notification shown upon successful readlist update',
                defaultMessage: 'Read list books updated: {readlist}',
                id: 't38TG7',
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
