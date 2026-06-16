import { useCurrentUser } from '@/colada/users'
import { useIntl } from 'vue-intl'
import { storeToRefs } from 'pinia'
import { useDialogsStore } from '@/stores/dialogs'
import { useDisplay } from 'vuetify/framework'
import { commonMessages } from '@/utils/i18n/common-messages'
import { type Action, LibrariesAction } from '@/types/action'
import { useEmptyTrashLibrary, useLibraries, useScanLibrary } from '@/colada/libraries'
import { useAppStore } from '@/stores/app'

export function useLibrariesActions(callback: (action: LibrariesAction) => void = () => {}) {
  const { isAdmin } = useCurrentUser()
  const { data: libraries } = useLibraries()
  const appStore = useAppStore()
  const intl = useIntl()
  const { confirm: dialogConfirm } = storeToRefs(useDialogsStore())
  const display = useDisplay()

  const actions = computed<Action<LibrariesAction>[]>(() => [
    {
      title: intl.formatMessage({
        description: 'Libraries menu: reorder or pin',
        defaultMessage: 'Reorder or pin',
        id: 'zuhGGz',
      }),
      action: LibrariesAction.REORDER,
      onClick: () => {
        appStore.reorderLibraries = true
        callback(LibrariesAction.REORDER)
      },
    },
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage({
              description: 'Libraries menu: scan',
              defaultMessage: 'Scan all libraries',
              id: 'CY8sfH',
            }),
            action: LibrariesAction.SCAN_ALL,
            onClick: () => {
              scanAll()
              callback(LibrariesAction.SCAN_ALL)
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage({
              description: 'Libraries menu: empty trash',
              defaultMessage: 'Empty trash for all libraries',
              id: 'CwteMk',
            }),
            action: LibrariesAction.EMPTY_TRASH_ALL,
            onMouseenter: (event: Event) =>
              (dialogConfirm.value.activator = event.currentTarget as Element),
            onClick: () => {
              emptyTrashAll()
              callback(LibrariesAction.EMPTY_TRASH_ALL)
            },
          },
        ]
      : []),
  ])

  //region Scan
  const { mutate: mutateScan } = useScanLibrary()

  function scanAll() {
    libraries.value?.forEach((it) => mutateScan({ libraryId: it.id }))
  }
  //endregion

  //region Empty Trash
  const { mutate: mutateEmptyTrash } = useEmptyTrashLibrary()

  function emptyTrashAll() {
    dialogConfirm.value.dialogProps = {
      title: intl.formatMessage(commonMessages.dialogEmptyTrashTitle),
      maxWidth: 600,
      mode: 'click',
      color: 'primary',
      okText: intl.formatMessage(commonMessages.dialogEmptyTrashConfirm),
      closeOnSave: true,
      fullscreen: display.xs.value,
    }
    dialogConfirm.value.slotWarning = {
      component: markRaw(h('div', intl.formatMessage(commonMessages.dialogEmptyTrashNotice))),
      props: {},
    }
    dialogConfirm.value.callback = () => {
      libraries.value?.forEach((it) => mutateEmptyTrash(it.id))
    }
  }
  //endregion

  return {
    actions: actions,
  }
}
