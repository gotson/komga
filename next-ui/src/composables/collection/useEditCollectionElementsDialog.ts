import { storeToRefs } from 'pinia'
import { type DialogResult, useDialogsStore } from '@/stores/dialogs'
import { useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify/framework'
import { useMessagesStore } from '@/stores/messages'

import CollectionOrganize from '@/components/collection/Organize.vue'
import { commonMessages } from '@/utils/i18n/common-messages'
import type { CollectionDto } from '@/generated/openapi'
import { useUpdateCollection } from '@/colada/collections'
import { pick } from '@/functions/object'

export function useEditCollectionElementsDialog() {
  const { confirmEdit: dialogConfirmEdit } = storeToRefs(useDialogsStore())
  const intl = useIntl()
  const display = useDisplay()
  const messagesStore = useMessagesStore()
  const { mutateAsync: mutateUpdateCollection } = useUpdateCollection()

  const prepareDialog = (collection: CollectionDto, callback: () => void = () => {}) => {
    dialogConfirmEdit.value.dialogProps = {
      title: intl.formatMessage({
        description: 'Edit readlist elements dialog title',
        defaultMessage: 'Organize read list',
        id: 'r48oBl',
      }),
      subtitle: collection.name,
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
        component: markRaw(CollectionOrganize),
      },
    }
    dialogConfirmEdit.value.record = collection
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

      const updatedData = dialogConfirmEdit.value.record as CollectionDto

      // update read list
      const updateDto = pick(updatedData, 'seriesIds')
      mutateUpdateCollection({ collectionId: collection.id, data: updateDto })
        .then(() => {
          hideDialog()
          messagesStore.messages.push({
            message: intl.formatMessage(
              {
                description: 'Snackbar notification shown upon successful collection update',
                defaultMessage: 'Collection series updated: {collection}',
                id: 'z4ywpN',
              },
              {
                collection: updatedData.name,
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
