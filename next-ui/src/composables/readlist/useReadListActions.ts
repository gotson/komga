import type { components } from '@/generated/openapi/komga'
import type { ErrorCause } from '@/api/komga-client'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useDialogsStore } from '@/stores/dialogs'
import { storeToRefs } from 'pinia'
import { useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify'
import { useMessagesStore } from '@/stores/messages'
import { useCurrentUser } from '@/colada/users'
import ReadListDeletionWarning from '@/components/readlist/DeletionWarning.vue'
import { ReadListAction } from '@/types/action/readlist'
import { useEditReadListDialog } from '@/composables/readlist/useEditReadListDialog'
import { useDeleteReadList } from '@/colada/readlists'
import { UserRoles } from '@/types/UserRoles'
import { readListFileUrl } from '@/api/files'
import type { Action } from '@/types/action/action'

export function useReadListActions(
  readList: MaybeRefOrGetter<components['schemas']['ReadListDto']>,
  callback: (action: ReadListAction) => void = () => {},
) {
  const { isAdmin, hasRole } = useCurrentUser()
  const intl = useIntl()
  const { confirm: dialogConfirm } = storeToRefs(useDialogsStore())
  const messagesStore = useMessagesStore()
  const display = useDisplay()

  const actions = computed<Action<ReadListAction>[]>(() => [
    ...(hasRole(UserRoles.FILE_DOWNLOAD)
      ? [
          {
            title: intl.formatMessage({
              description: 'Readlist menu: download',
              defaultMessage: 'Download',
              id: 'piQuKd',
            }),
            action: ReadListAction.DOWNLOAD,
            href: readListFileUrl(toValue(readList).id),
            onClick: () => {
              callback(ReadListAction.DOWNLOAD)
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage({
              description: 'Readlist menu: manage > edit',
              defaultMessage: 'Edit',
              id: 'ITAI2D',
            }),
            icon: 'i-mdi:pencil',
            action: ReadListAction.EDIT,
            onMouseenter: (event: Event) => (editActivator.value = event.currentTarget as Element),
            onClick: () => {
              edit()
              callback(ReadListAction.EDIT)
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage({
              description: 'Readlist menu: manage > delete',
              defaultMessage: 'Delete',
              id: 'YP5tSl',
            }),
            action: ReadListAction.DELETE,
            onMouseenter: (event: Event) =>
              (dialogConfirm.value.activator = event.currentTarget as Element),
            onClick: () => {
              deleteReadList()
              callback(ReadListAction.DELETE)
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
            text: intl.formatMessage(
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
    actions: actions,
  }
}
