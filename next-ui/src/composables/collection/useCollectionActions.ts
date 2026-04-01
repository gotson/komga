import type { components } from '@/generated/openapi/komga'
import type { ErrorCause } from '@/api/komga-client'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useDialogsStore } from '@/stores/dialogs'
import { storeToRefs } from 'pinia'
import { useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify'
import { useMessagesStore } from '@/stores/messages'
import { useCurrentUser } from '@/colada/users'
import CollectionDeletionWarning from '@/components/collection/DeletionWarning.vue'
import { CollectionAction } from '@/types/collection'
import { useEditCollectionDialog } from '@/composables/collection/useEditCollectionDialog'
import { useDeleteCollection } from '@/colada/collections'

export function useCollectionActions(
  collection: components['schemas']['CollectionDto'],
  callback: (action: CollectionAction) => void = () => {},
) {
  const { isAdmin } = useCurrentUser()
  const intl = useIntl()
  const { confirm: dialogConfirm } = storeToRefs(useDialogsStore())
  const messagesStore = useMessagesStore()
  const display = useDisplay()

  const manageActions = computed(() => [
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage({
              description: 'Collection menu: manage > edit',
              defaultMessage: 'Edit',
              id: '39afQA',
            }),
            action: CollectionAction.EDIT,
            onMouseenter: (event: Event) => (editActivator.value = event.currentTarget as Element),
            onClick: () => {
              edit()
              callback(CollectionAction.EDIT)
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage({
              description: 'Collection menu: manage > delete',
              defaultMessage: 'Delete',
              id: 'Ekh3wO',
            }),
            action: CollectionAction.DELETE,
            onMouseenter: (event: Event) =>
              (dialogConfirm.value.activator = event.currentTarget as Element),
            onClick: () => {
              deleteCollection()
              callback(CollectionAction.DELETE)
            },
          },
        ]
      : []),
  ])

  //region Edit collection
  const { prepareDialog: showEditCollectionDialog, activator: editActivator } =
    useEditCollectionDialog()

  function edit() {
    showEditCollectionDialog(collection)
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
      subtitle: collection.name,
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
      mutateDelete(collection.id)
        .then(() => {
          messagesStore.messages.push({
            text: intl.formatMessage(
              {
                description: 'Snackbar notification shown upon successful collection deletion',
                defaultMessage: 'Collection deleted: {collection}',
                id: 'HdsnFp',
              },
              {
                collection: collection.name,
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
        })
    }
  }
  //endregion

  return {
    // actions: actions,
    manageActions: manageActions,
  }
}
