import { storeToRefs } from 'pinia'
import { useDialogsStore } from '@/stores/dialogs'
import { useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify/framework'
import { useMessagesStore } from '@/stores/messages'

import EditCollection from '@/components/collection/form/Edit.vue'
import { commonMessages } from '@/utils/i18n/common-messages'
import {
  useAddCollectionPoster,
  useDeleteCollectionPoster,
  useMarkCollectionPosterSelected,
  useUpdateCollection,
} from '@/colada/collections'
import type { CollectionDto } from '@/generated/openapi'
import { pick } from '@/functions/pick'
import { createEntityUpdate, type EntityUpdate } from '@/functions/poster'

export function useEditCollectionDialog() {
  const { confirmEdit: dialogConfirmEdit } = storeToRefs(useDialogsStore())
  const intl = useIntl()
  const display = useDisplay()
  const messagesStore = useMessagesStore()
  const { mutateAsync: mutateUpdateCollection } = useUpdateCollection()

  const prepareDialog = (collection: CollectionDto, callback: () => void = () => {}) => {
    dialogConfirmEdit.value.dialogProps = {
      title: intl.formatMessage({
        description: 'Edit collection dialog title',
        defaultMessage: 'Edit collection',
        id: 'YVQ49g',
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
    dialogConfirmEdit.value.slot = {
      component: markRaw(EditCollection),
    }
    dialogConfirmEdit.value.record = createEntityUpdate(collection)
    dialogConfirmEdit.value.callback = async (
      hideDialog: () => void,
      setLoading: (isLoading: boolean) => void,
    ) => {
      setLoading(true)

      const updatedData = dialogConfirmEdit.value.record as EntityUpdate<CollectionDto>

      // upload new posters
      if (updatedData.uploadQueue.length > 0) {
        const { mutateAsync } = useAddCollectionPoster()
        for (const newPoster of updatedData.uploadQueue) {
          await mutateAsync({
            collectionId: updatedData.entity.id,
            file: newPoster.file,
            selected: newPoster.selected,
          }).catch((error) => {
            messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
          })
        }
      }

      // mark existing poster as selected
      if (updatedData.selected) {
        const { mutateAsync } = useMarkCollectionPosterSelected()
        await mutateAsync({
          collectionId: updatedData.entity.id,
          thumbnailId: updatedData.selected.id,
        }).catch((error) => {
          messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
        })
      }

      // delete posters
      if (updatedData.deleteQueue.length > 0) {
        const { mutateAsync } = useDeleteCollectionPoster()
        for (const posterToDelete of updatedData.deleteQueue) {
          await mutateAsync({
            collectionId: updatedData.entity.id,
            thumbnailId: posterToDelete.id,
          }).catch((error) => {
            messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
          })
        }
      }

      // update collection
      const updateDto = pick(updatedData.entity, 'name', 'ordered')
      mutateUpdateCollection({ collectionId: collection.id, data: updateDto })
        .then(() => {
          hideDialog()
          messagesStore.messages.push({
            message: intl.formatMessage(
              {
                description: 'Snackbar notification shown upon successful collection update',
                defaultMessage: 'Collection updated: {collection}',
                id: 'E0cw62',
              },
              {
                collection: updateDto.name,
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
