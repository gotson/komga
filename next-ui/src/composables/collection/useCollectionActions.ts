import { commonMessages } from '@/utils/i18n/common-messages'
import { useDialogsStore } from '@/stores/dialogs'
import { storeToRefs } from 'pinia'
import { useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify'
import { useMessagesStore } from '@/stores/messages'
import { useCurrentUser } from '@/colada/users'
import CollectionDeletionWarning from '@/components/collection/DeletionWarning.vue'
import { useEditCollectionDialog } from '@/composables/collection/useEditCollectionDialog'
import { useDeleteCollection } from '@/colada/collections'
import { type Action, actionDetails, ActionName } from '@/types/action/action'
import type { CollectionDto } from '@/generated/openapi'

export function useCollectionActions(
  collection: MaybeRefOrGetter<CollectionDto>,
  callback: (action: ActionName) => void = () => {},
) {
  const { isAdmin } = useCurrentUser()
  const intl = useIntl()
  const { confirm: dialogConfirm } = storeToRefs(useDialogsStore())
  const messagesStore = useMessagesStore()
  const display = useDisplay()

  const actions = computed<Action<ActionName>[]>(() => [
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage(actionDetails[ActionName.EditCollection].message),
            icon: actionDetails[ActionName.EditCollection].icon,
            action: ActionName.EditCollection,
            onMouseenter: (event: Event) => (editActivator.value = event.currentTarget as Element),
            onClick: () => {
              edit(() => callback(ActionName.EditCollection))
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage(actionDetails[ActionName.Delete].message),
            action: ActionName.Delete,
            onMouseenter: (event: Event) =>
              (dialogConfirm.value.activator = event.currentTarget as Element),
            onClick: () => {
              deleteCollection()
            },
          },
        ]
      : []),
  ])

  //region Edit collection
  const { prepareDialog: showEditCollectionDialog, activator: editActivator } =
    useEditCollectionDialog()

  function edit(callback: () => void) {
    showEditCollectionDialog(toValue(collection), callback)
  }
  //endregion

  //region Delete
  const { mutateAsync: mutateDelete } = useDeleteCollection()

  function deleteCollection() {
    dialogConfirm.value.dialogProps = {
      title: intl.formatMessage({
        description: 'Collection delete dialog: title',
        defaultMessage: 'Delete collection',
        id: 'k6BCzW',
      }),
      subtitle: toValue(collection).name,
      maxWidth: 600,
      mode: 'checkbox',
      color: 'error',
      okText: intl.formatMessage({
        description: 'Collection delete dialog: confirm button',
        defaultMessage: 'Delete',
        id: 'rDBhmQ',
      }),
      closeOnSave: true,
      fullscreen: display.xs.value,
    }
    dialogConfirm.value.slotWarning = {
      component: markRaw(CollectionDeletionWarning),
      props: {},
    }
    dialogConfirm.value.callback = () => {
      mutateDelete(toValue(collection).id)
        .then(() => {
          messagesStore.messages.push({
            message: intl.formatMessage(
              {
                description: 'Snackbar notification shown upon successful collection deletion',
                defaultMessage: 'Collection deleted: {collection}',
                id: 'HdsnFp',
              },
              {
                collection: toValue(collection).name,
              },
            ),
          })
        })
        .catch((error) => {
          messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
        })
      callback(ActionName.Delete)
    }
  }
  //endregion

  return {
    actions,
  }
}
