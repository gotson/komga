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
import { BookAction } from '@/types/action/book'
import {
  useAnalyzeBook,
  useDeleteBook,
  useMarkBookRead,
  useMarkBookUnread,
  useRefreshMetadataBook,
} from '@/colada/books'
import { useEditBookMetadataDialog } from '@/composables/book/useEditBookMetadataDialog'
import { UserRoles } from '@/types/UserRoles'
import { bookFileUrl } from '@/api/files'
import type { Action } from '@/types/action/action'
import { useBook } from '@/composables/book/useBook'
import { useBookReadProgress } from '@/composables/book/useBookReadProgress'
import { bookReaderUrl } from '@/api/links'

export function useBookActions(
  book: MaybeRefOrGetter<components['schemas']['BookDto']>,
  callback: (action: BookAction) => void = () => {},
) {
  const { isAdmin, hasRole } = useCurrentUser()
  const intl = useIntl()
  const { confirm: dialogConfirm } = storeToRefs(useDialogsStore())
  const messagesStore = useMessagesStore()
  const display = useDisplay()
  const { canRead, isEpubReader } = useBook(book)
  const { inProgress } = useBookReadProgress(book)

  const actions = computed<Action<BookAction>[]>(() => [
    ...(isAdmin.value && toValue(book).oneshot
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
    ...(!toValue(book).readProgress?.completed
      ? [
          {
            title: intl.formatMessage({
              description: 'Book menu: mark as read',
              defaultMessage: 'Mark as read',
              id: 'lFGLru',
            }),
            icon: 'i-mdi:book-check-outline',
            action: BookAction.MARK_READ,
            onClick: () => {
              markRead()
              callback(BookAction.MARK_READ)
            },
          },
        ]
      : []),
    ...(!!toValue(book).readProgress
      ? [
          {
            title: intl.formatMessage({
              description: 'Book menu: mark as unread',
              defaultMessage: 'Mark as unread',
              id: 'a+9yUi',
            }),
            icon: 'i-mdi:book-remove-outline',
            action: BookAction.MARK_UNREAD,
            onClick: () => {
              markUnread()
              callback(BookAction.MARK_UNREAD)
            },
          },
        ]
      : []),
    ...(hasRole(UserRoles.FILE_DOWNLOAD)
      ? [
          {
            title: intl.formatMessage({
              description: 'Book menu: download',
              defaultMessage: 'Download',
              id: 'R1n0du',
            }),
            action: BookAction.DOWNLOAD,
            href: bookFileUrl(toValue(book).id),
            onClick: () => {
              callback(BookAction.DOWNLOAD)
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage({
              description: 'Book menu: manage > edit metadata',
              defaultMessage: 'Edit metadata',
              id: 'M5GJZQ',
            }),
            icon: 'i-mdi:pencil',
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
    {
      title: inProgress.value
        ? intl.formatMessage({
            description: 'Book menu: resume reading button',
            defaultMessage: 'Resume',
            id: 'cIM3eJ',
          })
        : intl.formatMessage({
            description: 'Book menu: read button',
            defaultMessage: 'Read',
            id: 'Y8SsFO',
          }),
      icon: 'i-mdi:play',
      action: BookAction.OPEN_READER,
      disabled: !canRead.value,
      onClick: () => {
        window.open(bookReaderUrl(toValue(book).id, isEpubReader.value), '_blank')
        callback(BookAction.OPEN_READER)
      },
    },
    {
      title: intl.formatMessage({
        description: 'Series view: read incognito button: tooltip',
        defaultMessage: 'Private reading session',
        id: 'DLUIbm',
      }),
      icon: 'i-mdi:incognito',
      action: BookAction.OPEN_READER_INCOGNITO,
      disabled: !canRead.value,
      onClick: () => {
        window.open(bookReaderUrl(toValue(book).id, isEpubReader.value, true), '_blank')
        callback(BookAction.OPEN_READER_INCOGNITO)
      },
    },
  ])

  //region Update Series metadata
  const { prepareDialog: showEditBookMetadataDialog, activator: editMetadataActivator } =
    useEditBookMetadataDialog()

  function updateBookMetadata() {
    showEditBookMetadataDialog(toValue(book))
  }
  //endregion

  //region Refresh Metadata
  const { mutate: mutateRefreshMetadata } = useRefreshMetadataBook()

  function refreshMetadata() {
    mutateRefreshMetadata(toValue(book).id)
  }
  //endregion

  //region Analyze
  const { mutate: mutateAnalyze } = useAnalyzeBook()

  function analyzeBook() {
    mutateAnalyze(toValue(book).id)
  }
  //endregion

  //TODO: do :)
  function todo() {}

  //region Mark read
  const { mutate: mutateMarkRead } = useMarkBookRead()

  function markRead() {
    mutateMarkRead(toValue(book).id)
  }
  //endregion

  //region Mark unread
  const { mutate: mutateMarkUnread } = useMarkBookUnread()

  function markUnread() {
    mutateMarkUnread(toValue(book).id)
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
      subtitle: toValue(book).metadata.title,
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
      mutateDelete(toValue(book).id)
        .then(() => {
          messagesStore.messages.push({
            text: intl.formatMessage(
              {
                description: 'Snackbar notification shown upon successful book files deletion',
                defaultMessage: 'Book files deleted: {book}',
                id: 'ccDES8',
              },
              {
                book: toValue(book).metadata.title,
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
