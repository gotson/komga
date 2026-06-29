import { commonMessages } from '@/utils/i18n/common-messages'
import { useDialogsStore } from '@/stores/dialogs'
import { storeToRefs } from 'pinia'
import { useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify'
import { useMessagesStore } from '@/stores/messages'
import { useCurrentUser } from '@/colada/users'
import ReadListDeletionWarning from '@/components/readlist/DeletionWarning.vue'
import { useEditReadListDialog } from '@/composables/readlist/useEditReadListDialog'
import { useDeleteReadList } from '@/colada/readlists'
import { UserRoles } from '@/types/UserRoles'
import { readListFileUrl } from '@/api/files'
import { type Action, actionDetails, ActionName } from '@/types/action/action'
import type { ReadListDto } from '@/generated/openapi'

export function useReadListActions(
  readList: MaybeRefOrGetter<ReadListDto>,
  callback: (action: ActionName) => void = () => {},
) {
  const { isAdmin, hasRole } = useCurrentUser()
  const intl = useIntl()
  const { confirm: dialogConfirm } = storeToRefs(useDialogsStore())
  const messagesStore = useMessagesStore()
  const display = useDisplay()

  const actions = computed<Action<ActionName>[]>(() => [
    ...(hasRole(UserRoles.FILE_DOWNLOAD)
      ? [
          {
            title: intl.formatMessage(actionDetails[ActionName.DOWNLOAD].message),
            action: ActionName.DOWNLOAD,
            href: readListFileUrl(toValue(readList).id),
            onClick: () => {
              callback(ActionName.DOWNLOAD)
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage(actionDetails[ActionName.EDIT_READLIST].message),
            icon: actionDetails[ActionName.EDIT_READLIST].icon,
            action: ActionName.EDIT_READLIST,
            onMouseenter: (event: Event) => (editActivator.value = event.currentTarget as Element),
            onClick: () => {
              edit()
              callback(ActionName.EDIT_READLIST) //TODO: move callback after dialog validation
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage(actionDetails[ActionName.DELETE].message),
            action: ActionName.DELETE,
            onMouseenter: (event: Event) =>
              (dialogConfirm.value.activator = event.currentTarget as Element),
            onClick: () => {
              deleteReadList()
            },
          },
        ]
      : []),
  ])

  //region Edit collection
  const { prepareDialog: showEditDialog, activator: editActivator } = useEditReadListDialog()

  function edit() {
    showEditDialog(toValue(readList))
  }
  //endregion

  //region Delete
  const { mutateAsync: mutateDelete } = useDeleteReadList()

  function deleteReadList() {
    dialogConfirm.value.dialogProps = {
      title: intl.formatMessage({
        description: 'Readlist delete dialog: title',
        defaultMessage: 'Delete read list',
        id: 'a5jT6x',
      }),
      subtitle: toValue(readList).name,
      maxWidth: 600,
      mode: 'checkbox',
      color: 'error',
      okText: intl.formatMessage({
        description: 'Readlist delete dialog: confirm button',
        defaultMessage: 'Delete',
        id: 'vTqlcG',
      }),
      closeOnSave: true,
      fullscreen: display.xs.value,
    }
    dialogConfirm.value.slotWarning = {
      component: markRaw(ReadListDeletionWarning),
      props: {},
    }
    dialogConfirm.value.callback = () => {
      mutateDelete(toValue(readList).id)
        .then(() => {
          messagesStore.messages.push({
            message: intl.formatMessage(
              {
                description: 'Snackbar notification shown upon successful readlist deletion',
                defaultMessage: 'Read list deleted: {readlist}',
                id: 'Oj3xqB',
              },
              {
                readlist: toValue(readList).name,
              },
            ),
          })
        })
        .catch((error) => {
          messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
        })
      callback(ActionName.DELETE)
    }
  }
  //endregion

  return {
    actions: actions,
  }
}
