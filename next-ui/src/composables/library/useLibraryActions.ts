import { useCurrentUser } from '@/colada/users'
import { useIntl } from 'vue-intl'
import { storeToRefs } from 'pinia'
import { useDialogsStore } from '@/stores/dialogs'
import { useMessagesStore } from '@/stores/messages'
import { useDisplay } from 'vuetify/framework'
import { commonMessages } from '@/utils/i18n/common-messages'
import { type Action } from '@/types/action/action'
import LibraryDeletionWarning from '@/components/library/DeletionWarning.vue'
import CreateEdit from '@/components/library/form/CreateEdit.vue'
import {
  useAnalyzeLibrary,
  useDeleteLibrary,
  useEmptyTrashLibrary,
  useRefreshMetadataLibrary,
  useScanLibrary,
  useUpdateLibrary,
} from '@/colada/libraries'
import { LibraryAction } from '@/types/action/library'
import type { LibraryDto } from '@/generated/openapi'

export function useLibraryActions(
  library: MaybeRefOrGetter<LibraryDto>,
  callback: (action: LibraryAction) => void = () => {},
) {
  const { isAdmin } = useCurrentUser()
  const intl = useIntl()
  const { confirmEdit: dialogConfirmEdit, confirm: dialogConfirm } = storeToRefs(useDialogsStore())
  const messagesStore = useMessagesStore()
  const display = useDisplay()

  const actions = computed<Action<LibraryAction>[]>(() =>
    !isAdmin
      ? []
      : [
          {
            title: intl.formatMessage({
              description: 'Library menu: scan',
              defaultMessage: 'Scan library files',
              id: 'GCwZB2',
            }),
            action: LibraryAction.SCAN,
            onClick: () => {
              scanLibrary()
            },
          },
          {
            title: intl.formatMessage({
              description: 'Library menu: manage > edit',
              defaultMessage: 'Edit',
              id: 'n4w2CE',
            }),
            action: LibraryAction.EDIT,
            onMouseenter: (event: Event) =>
              (dialogConfirmEdit.value.activator = event.currentTarget as Element),
            onClick: () => {
              updateLibrary()
            },
          },
          {
            title: intl.formatMessage({
              description: 'Library menu: manage > deep scan',
              defaultMessage: 'Deep scan',
              id: 'foSDIW',
            }),
            action: LibraryAction.SCAN_DEEP,
            onMouseenter: (event: Event) =>
              (dialogConfirm.value.activator = event.currentTarget as Element),
            onClick: () => {
              scanDeep()
            },
          },
          {
            title: intl.formatMessage({
              description: 'Library menu: manage > refresh all metadata',
              defaultMessage: 'Refresh all metadata',
              id: 'OUyleX',
            }),
            action: LibraryAction.REFRESH_METADATA,
            onMouseenter: (event: Event) =>
              (dialogConfirm.value.activator = event.currentTarget as Element),
            onClick: () => {
              refreshMetadata()
            },
          },
          {
            title: intl.formatMessage({
              description: 'Library menu: manage > empty trash',
              defaultMessage: 'Empty trash',
              id: 'sdNz1F',
            }),
            action: LibraryAction.EMPTY_TRASH,
            onMouseenter: (event: Event) =>
              (dialogConfirm.value.activator = event.currentTarget as Element),
            onClick: () => {
              emptyTrash()
            },
          },
          {
            title: intl.formatMessage({
              description: 'Library menu: manage > analyze',
              defaultMessage: 'Analyze',
              id: 'E5ZMyt',
            }),
            action: LibraryAction.ANALYZE,
            onMouseenter: (event: Event) =>
              (dialogConfirm.value.activator = event.currentTarget as Element),
            onClick: () => {
              analyzeLibrary()
            },
          },
          {
            title: intl.formatMessage({
              description: 'Library menu: manage > delete',
              defaultMessage: 'Delete',
              id: 'LFf8QB',
            }),
            action: LibraryAction.DELETE,
            onMouseenter: (event: Event) =>
              (dialogConfirm.value.activator = event.currentTarget as Element),
            onClick: () => {
              deleteLibrary()
            },
          },
        ],
  )

  //region Update Library
  const { mutateAsync: mutateUpdateLibrary } = useUpdateLibrary()

  function updateLibrary() {
    dialogConfirmEdit.value.dialogProps = {
      title: intl.formatMessage({
        description: 'Update library dialog title',
        defaultMessage: 'Update library',
        id: 'am3r7e',
      }),
      maxWidth: 600,
      okText: 'Save',
      cardTextClass: 'px-0',
      closeOnSave: false,
      scrollable: true,
      fullscreen: display.xs.value,
    }
    dialogConfirmEdit.value.slot = {
      component: markRaw(CreateEdit),
      props: { createMode: false },
    }
    dialogConfirmEdit.value.record = toValue(library)
    dialogConfirmEdit.value.callback = (
      hideDialog: () => void,
      setLoading: (isLoading: boolean) => void,
    ) => {
      setLoading(true)

      const updatedLib = dialogConfirmEdit.value.record as LibraryDto

      mutateUpdateLibrary(updatedLib)
        .then(() => {
          hideDialog()
          messagesStore.messages.push({
            message: intl.formatMessage(
              {
                description: 'Snackbar notification shown upon successful library update',
                defaultMessage: 'Library updated: {library}',
                id: 'aOiU5f',
              },
              {
                library: updatedLib.name,
              },
            ),
          })
        })
        .catch((error) => {
          messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
          setLoading(false)
        })
      callback(LibraryAction.EDIT)
    }
  }
  //endregion

  //region Refresh Metadata
  const { mutate: mutateRefreshMetadata } = useRefreshMetadataLibrary()

  function refreshMetadata() {
    dialogConfirm.value.dialogProps = {
      title: intl.formatMessage({
        description: 'Library refresh metadata dialog: title',
        defaultMessage: 'Refresh all metadata',
        id: 'xiyw1J',
      }),
      subtitle: toValue(library).name,
      maxWidth: 600,
      mode: 'click',
      color: 'primary',
      okText: intl.formatMessage({
        description: 'Library refresh metadata dialog: confirm button',
        defaultMessage: 'Refresh',
        id: 'i+kSy9',
      }),
      closeOnSave: true,
      fullscreen: display.xs.value,
    }
    dialogConfirm.value.slotWarning = {
      component: markRaw(
        h(
          'div',
          intl.formatMessage({
            description: 'Library refresh metadata dialog: warning text',
            defaultMessage:
              'Refreshes metadata for all the media files in the library. Depending on your library size, this may take a long time.',
            id: 'vs88Ef',
          }),
        ),
      ),
      props: {},
    }
    dialogConfirm.value.callback = () => {
      mutateRefreshMetadata(toValue(library).id)
      callback(LibraryAction.REFRESH_METADATA)
    }
  }
  //endregion

  //region Empty Trash
  const { mutate: mutateEmptyTrash } = useEmptyTrashLibrary()

  function emptyTrash() {
    dialogConfirm.value.dialogProps = {
      title: intl.formatMessage(commonMessages.dialogEmptyTrashTitle),
      subtitle: toValue(library).name,
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
      mutateEmptyTrash(toValue(library).id)
      callback(LibraryAction.EMPTY_TRASH)
    }
  }
  //endregion

  //region Analyze
  const { mutate: mutateAnalyze } = useAnalyzeLibrary()

  function analyzeLibrary() {
    dialogConfirm.value.dialogProps = {
      title: intl.formatMessage({
        description: 'Library analyze dialog: title',
        defaultMessage: 'Analyze library',
        id: '5JGOUU',
      }),
      subtitle: toValue(library).name,
      maxWidth: 600,
      mode: 'click',
      color: 'primary',
      okText: intl.formatMessage({
        description: 'Library analyze dialog: confirm button',
        defaultMessage: 'Analyze',
        id: 'jN3N1Q',
      }),
      closeOnSave: true,
      fullscreen: display.xs.value,
    }
    dialogConfirm.value.slotWarning = {
      component: markRaw(
        h(
          'div',
          intl.formatMessage({
            description: 'Library empty trash dialog: warning text',
            defaultMessage:
              'Analyzes all the media files in the library. The analysis captures information about the media. Depending on your library size, this may take a long time.',
            id: '8xonXJ',
          }),
        ),
      ),
      props: {},
    }
    dialogConfirm.value.callback = () => {
      mutateAnalyze(toValue(library).id)
      callback(LibraryAction.ANALYZE)
    }
  }
  //endregion

  //region Scan
  const { mutate: mutateScan } = useScanLibrary()

  function scanLibrary() {
    mutateScan({ libraryId: toValue(library).id })
    callback(LibraryAction.SCAN)
  }

  function scanDeep() {
    dialogConfirm.value.dialogProps = {
      title: intl.formatMessage({
        description: 'Library deep scan dialog: title',
        defaultMessage: 'Deep scan',
        id: 'hV3EW+',
      }),
      subtitle: toValue(library).name,
      maxWidth: 600,
      mode: 'click',
      color: 'primary',
      okText: intl.formatMessage({
        description: 'Library deep scan: confirm button',
        defaultMessage: 'Deep scan',
        id: 'OxqfKF',
      }),
      closeOnSave: true,
      fullscreen: display.xs.value,
    }
    dialogConfirm.value.slotWarning = {
      component: markRaw(
        h(
          'div',
          intl.formatMessage({
            description: 'Library deep scan dialog: warning text',
            defaultMessage:
              'Performs a deep scan of the library files. Depending on your library size, this may take a long time.',
            id: 'y3nPgO',
          }),
        ),
      ),
      props: {},
    }
    dialogConfirm.value.callback = () => {
      mutateScan({ libraryId: toValue(library).id, deep: true })
      callback(LibraryAction.SCAN_DEEP)
    }
  }
  //endregion

  //region Delete
  const { mutateAsync: mutateDelete } = useDeleteLibrary()

  function deleteLibrary() {
    dialogConfirm.value.dialogProps = {
      title: intl.formatMessage({
        description: 'Library delete dialog: title',
        defaultMessage: 'Delete library',
        id: '3T1ln7',
      }),
      subtitle: toValue(library).name,
      maxWidth: 600,
      mode: 'textinput',
      color: 'error',
      validateText: toValue(library).name,
      okText: intl.formatMessage({
        description: 'Library delete dialog: confirm button',
        defaultMessage: 'Delete',
        id: '/5Gb4y',
      }),
      closeOnSave: true,
      fullscreen: display.xs.value,
    }
    dialogConfirm.value.slotWarning = {
      component: markRaw(LibraryDeletionWarning),
      props: {},
    }
    dialogConfirm.value.callback = () => {
      mutateDelete(toValue(library).id)
        .then(() => {
          messagesStore.messages.push({
            message: intl.formatMessage(
              {
                description: 'Snackbar notification shown upon successful library deletion',
                defaultMessage: 'Library deleted: {library}',
                id: 'PvKF7E',
              },
              {
                library: toValue(library).name,
              },
            ),
          })
        })
        .catch((error) => {
          messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
        })
      callback(LibraryAction.DELETE)
    }
  }
  //endregion

  return {
    actions: actions,
  }
}
