import { storeToRefs } from 'pinia'
import { type DialogResult, useDialogsStore } from '@/stores/dialogs'
import { useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify/framework'
import { useMessagesStore } from '@/stores/messages'

import EditReadList from '@/components/readlist/form/Edit.vue'
import { commonMessages } from '@/utils/i18n/common-messages'
import {
  useAddReadListPoster,
  useDeleteReadListPoster,
  useMarkReadListPosterSelected,
  useUpdateReadList,
} from '@/colada/readlists'
import type { ReadListDto } from '@/generated/openapi'
import { createEntityUpdate, type EntityUpdate } from '@/functions/poster'
import { pick } from '@/functions/pick'

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
      maxWidth: 900,
      cardTextProps: {
        class: 'px-0',
      },
      closeOnSave: false,
      scrollable: true,
      fullscreen: display.xs.value,
    }
    dialogConfirmEdit.value.slot = {
      component: markRaw(EditReadList),
    }
    dialogConfirmEdit.value.record = createEntityUpdate(readList)
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

      const updatedData = dialogConfirmEdit.value.record as EntityUpdate<ReadListDto, never>

      // upload new posters
      if (updatedData.uploadQueue.length > 0) {
        const { mutateAsync } = useAddReadListPoster()
        for (const newPoster of updatedData.uploadQueue) {
          await mutateAsync({
            readListId: updatedData.entity.id,
            file: newPoster.file,
            selected: newPoster.selected,
          }).catch((error) => {
            messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
          })
        }
      }

      // mark existing poster as selected
      if (updatedData.selected) {
        const { mutateAsync } = useMarkReadListPosterSelected()
        await mutateAsync({
          readListId: updatedData.entity.id,
          thumbnailId: updatedData.selected.id,
        }).catch((error) => {
          messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
        })
      }

      // delete posters
      if (updatedData.deleteQueue.length > 0) {
        const { mutateAsync } = useDeleteReadListPoster()
        for (const posterToDelete of updatedData.deleteQueue) {
          await mutateAsync({
            readListId: updatedData.entity.id,
            thumbnailId: posterToDelete.id,
          }).catch((error) => {
            messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
          })
        }
      }

      // update read list
      const updateDto = pick(updatedData.entity, 'name', 'ordered', 'summary')
      mutateUpdate({ readListId: readList.id, data: updateDto })
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
                readlist: updateDto.name,
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
