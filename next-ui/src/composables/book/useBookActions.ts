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
import { type Action, actionDetails, ActionName } from '@/types/action/action'
import { useBook } from '@/composables/book/useBook'
import { useBookReadProgress } from '@/composables/book/useBookReadProgress'
import { bookReaderUrl } from '@/api/links'

export function useBookActions(
  book: MaybeRefOrGetter<components['schemas']['BookDto']>,
  callback: (action: ActionName) => void = () => {},
) {
  const { isAdmin, hasRole } = useCurrentUser()
  const intl = useIntl()
  const { confirm: dialogConfirm } = storeToRefs(useDialogsStore())
  const messagesStore = useMessagesStore()
  const display = useDisplay()
  const { canRead, isEpubReader } = useBook(book)
  const { inProgress } = useBookReadProgress(book)

  const actions = computed<Action<ActionName>[]>(() => [
    ...(isAdmin.value && toValue(book).oneshot
      ? [
          {
            title: intl.formatMessage(actionDetails[ActionName.ADD_TO_COLLECTION].message),
            disabled: true, //TODO: implement
            action: ActionName.ADD_TO_COLLECTION,
            onClick: () => {
              todo()
              callback(ActionName.ADD_TO_COLLECTION)
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage(actionDetails[ActionName.ADD_TO_READLIST].message),
            disabled: true, //TODO: implement
            action: ActionName.ADD_TO_READLIST,
            onClick: () => {
              todo()
              callback(ActionName.ADD_TO_READLIST)
            },
          },
        ]
      : []),
    ...(!toValue(book).readProgress?.completed
      ? [
          {
            title: intl.formatMessage(actionDetails[ActionName.MARK_READ].message),
            icon: actionDetails[ActionName.MARK_READ].icon,
            action: ActionName.MARK_READ,
            onClick: () => {
              markRead()
            },
          },
        ]
      : []),
    ...(!!toValue(book).readProgress
      ? [
          {
            title: intl.formatMessage(actionDetails[ActionName.MARK_UNREAD].message),
            icon: actionDetails[ActionName.MARK_UNREAD].icon,
            action: ActionName.MARK_UNREAD,
            onClick: () => {
              markUnread()
            },
          },
        ]
      : []),
    ...(hasRole(UserRoles.FILE_DOWNLOAD)
      ? [
          {
            title: intl.formatMessage(actionDetails[ActionName.DOWNLOAD].message),
            action: ActionName.DOWNLOAD,
            href: bookFileUrl(toValue(book).id),
            onClick: () => {
              callback(ActionName.DOWNLOAD)
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage(actionDetails[ActionName.EDIT_BOOK].message),
            icon: actionDetails[ActionName.EDIT_BOOK].icon,
            action: ActionName.EDIT_BOOK,
            onMouseenter: (event: Event) =>
              (editMetadataActivator.value = event.currentTarget as Element),
            onClick: () => {
              updateBookMetadata()
              callback(ActionName.EDIT_BOOK) //TODO: move callback after dialog validation
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage(actionDetails[ActionName.REFRESH_METADATA].message),
            action: ActionName.REFRESH_METADATA,
            onClick: () => {
              refreshMetadata()
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage(actionDetails[ActionName.ANALYZE].message),

            action: ActionName.ANALYZE,
            onClick: () => {
              analyzeBook()
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
            action: ActionName.DELETE,
            onMouseenter: (event: Event) =>
              (dialogConfirm.value.activator = event.currentTarget as Element),
            onClick: () => {
              deleteBook()
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
      icon: actionDetails[ActionName.OPEN_READER].icon,
      action: ActionName.OPEN_READER,
      disabled: !canRead.value,
      onClick: () => {
        window.open(bookReaderUrl(toValue(book).id, isEpubReader.value), '_blank')
        callback(ActionName.OPEN_READER)
      },
    },
    {
      title: intl.formatMessage(actionDetails[ActionName.OPEN_READER_INCOGNITO].message),
      icon: actionDetails[ActionName.OPEN_READER_INCOGNITO].icon,
      action: ActionName.OPEN_READER_INCOGNITO,
      disabled: !canRead.value,
      onClick: () => {
        window.open(bookReaderUrl(toValue(book).id, isEpubReader.value, true), '_blank')
        callback(ActionName.OPEN_READER_INCOGNITO)
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
    callback(ActionName.REFRESH_METADATA)
  }
  //endregion

  //region Analyze
  const { mutate: mutateAnalyze } = useAnalyzeBook()

  function analyzeBook() {
    mutateAnalyze(toValue(book).id)
    callback(ActionName.ANALYZE)
  }
  //endregion

  //TODO: do :)
  function todo() {}

  //region Mark read
  const { mutate: mutateMarkRead } = useMarkBookRead()

  function markRead() {
    mutateMarkRead(toValue(book).id)
    callback(ActionName.MARK_READ)
  }
  //endregion

  //region Mark unread
  const { mutate: mutateMarkUnread } = useMarkBookUnread()

  function markUnread() {
    mutateMarkUnread(toValue(book).id)
    callback(ActionName.MARK_UNREAD)
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
      callback(ActionName.DELETE)
    }
  }
  //endregion

  return {
    actions: actions,
  }
}
