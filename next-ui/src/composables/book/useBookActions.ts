import type { components } from '@/generated/openapi/komga'
import { useCurrentUser } from '@/colada/users'
import { useIntl } from 'vue-intl'
import { storeToRefs } from 'pinia'
import { useDialogsStore } from '@/stores/dialogs'
import { useMessagesStore } from '@/stores/messages'
import { useDisplay } from 'vuetify/framework'
import BookDeletionWarning from '@/components/book/DeletionWarning.vue'
import type { ErrorCause } from '@/api/komga-client'
import { commonMessages } from '@/utils/i18n/common-messages'
import { BookAction } from '@/types/book'
import {
  useAnalyzeBook,
  useDeleteBook,
  useMarkBookRead,
  useMarkBookUnread,
  useRefreshMetadataBook,
} from '@/colada/books'
import { useEditSeriesMetadataDialog } from '@/composables/series/useEditSeriesMetadataDialog'
import { useEditBookMetadataDialog } from '@/composables/book/useEditBookMetadataDialog'

export function useBookActions(
  book: components['schemas']['BookDto'],
  callback: (action: BookAction) => void = () => {},
) {
  const { isAdmin } = useCurrentUser()
  const intl = useIntl()
  const { confirm: dialogConfirm } = storeToRefs(useDialogsStore())
  const messagesStore = useMessagesStore()
  const display = useDisplay()

  const actions = computed(() => [
    ...(isAdmin.value && book.oneshot
      ? [
          {
            title: intl.formatMessage({
              description: 'Book menu: add to collection',
              defaultMessage: 'Add to collection',
              id: 'yNNH8a',
            }),
            action: BookAction.ADD_TO_COLLECTION,
            onClick: () => {
              todo()
              callback(BookAction.ADD_TO_COLLECTION)
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage({
              description: 'Book menu: add to read list',
              defaultMessage: 'Add to read list',
              id: 'Q6H+z7',
            }),
            action: BookAction.ADD_TO_READLIST,
            onClick: () => {
              todo()
              callback(BookAction.ADD_TO_READLIST)
            },
          },
        ]
      : []),
    ...(!book.readProgress?.completed
      ? [
          {
            title: intl.formatMessage({
              description: 'Book menu: mark as read',
              defaultMessage: 'Mark as read',
              id: 'lFGLru',
            }),
            action: BookAction.MARK_READ,
            onClick: () => {
              markRead()
              callback(BookAction.MARK_READ)
            },
          },
        ]
      : []),
    ...(!!book.readProgress
      ? [
          {
            title: intl.formatMessage({
              description: 'Book menu: mark as unread',
              defaultMessage: 'Mark as unread',
              id: 'a+9yUi',
            }),
            action: BookAction.MARK_UNREAD,
            onClick: () => {
              markUnread()
              callback(BookAction.MARK_UNREAD)
            },
          },
        ]
      : []),
  ])

  const manageActions = computed(() => [
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage({
              description: 'Book menu: manage > edit metadata',
              defaultMessage: 'Edit metadata',
              id: 'M5GJZQ',
            }),
            action: BookAction.EDIT_METADATA,
            onMouseenter: (event: Event) =>
              (editMetadataActivator.value = event.currentTarget as Element),
            onClick: () => {
              updateBookMetadata()
              callback(BookAction.EDIT_METADATA)
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage({
              description: 'Book menu: manage > refresh metadata',
              defaultMessage: 'Refresh metadata',
              id: 'BdFv4r',
            }),
            action: BookAction.REFRESH_METADATA,
            onClick: () => {
              refreshMetadata()
              callback(BookAction.REFRESH_METADATA)
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage({
              description: 'Book menu: manage > analyze',
              defaultMessage: 'Analyze',
              id: 'J0TxGf',
            }),
            action: BookAction.ANALYZE,
            onClick: () => {
              analyzeBook()
              callback(BookAction.ANALYZE)
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage({
              description: 'Book menu: manage > delete file',
              defaultMessage: 'Delete file',
              id: 'vXhkpo',
            }),
            action: BookAction.DELETE_FILES,
            onMouseenter: (event: Event) =>
              (dialogConfirm.value.activator = event.currentTarget as Element),
            onClick: () => {
              deleteBook()
              callback(BookAction.DELETE_FILES)
            },
          },
        ]
      : []),
  ])

  //region Update Series metadata
  const { prepareDialog: showEditBookMetadataDialog, activator: editMetadataActivator } =
    useEditBookMetadataDialog()

  function updateBookMetadata() {
    showEditBookMetadataDialog(book)
  }
  //endregion

  //region Refresh Metadata
  const { mutate: mutateRefreshMetadata } = useRefreshMetadataBook()

  function refreshMetadata() {
    mutateRefreshMetadata(book.id)
  }
  //endregion

  //region Analyze
  const { mutate: mutateAnalyze } = useAnalyzeBook()

  function analyzeBook() {
    mutateAnalyze(book.id)
  }
  //endregion

  function todo() {}

  //region Mark read
  const { mutate: mutateMarkRead } = useMarkBookRead()

  function markRead() {
    mutateMarkRead(book.id)
  }
  //endregion

  //region Mark unread
  const { mutate: mutateMarkUnread } = useMarkBookUnread()

  function markUnread() {
    mutateMarkUnread(book.id)
  }
  //endregion

  //region Delete
  const { mutateAsync: mutateDelete } = useDeleteBook()

  function deleteBook() {
    dialogConfirm.value.dialogProps = {
      title: intl.formatMessage({
        description: 'Book delete dialog: title',
        defaultMessage: 'Delete book files',
        id: 'NhIart',
      }),
      subtitle: book.metadata.title,
      maxWidth: 600,
      mode: 'checkbox',
      color: 'error',
      okText: intl.formatMessage({
        description: 'Book delete dialog: confirm button',
        defaultMessage: 'Delete files',
        id: '8CVWWg',
      }),
      closeOnSave: true,
      fullscreen: display.xs.value,
    }
    dialogConfirm.value.slotWarning = {
      component: markRaw(BookDeletionWarning),
      props: {},
    }
    dialogConfirm.value.callback = () => {
      mutateDelete(book.id)
        .then(() => {
          messagesStore.messages.push({
            text: intl.formatMessage(
              {
                description: 'Snackbar notification shown upon successful book files deletion',
                defaultMessage: 'Book files deleted: {book}',
                id: 'ccDES8',
              },
              {
                book: book.metadata.title,
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
    manageActions: manageActions,
  }
}
