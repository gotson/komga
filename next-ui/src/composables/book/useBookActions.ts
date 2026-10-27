import { useCurrentUser } from '@/colada/users'
import { useIntl } from 'vue-intl'
import { storeToRefs } from 'pinia'
import { type DialogResult, useDialogsStore } from '@/stores/dialogs'
import { useMessagesStore } from '@/stores/messages'
import { useDisplay } from 'vuetify/framework'
import BookDeletionWarning from '@/components/book/DeletionWarning.vue'
import { commonMessages } from '@/utils/i18n/common-messages'
import {
  useAnalyzeBook,
  useDeleteBook,
  useMarkBookRead,
  useMarkBookUnread,
  useRefreshMetadataBook,
} from '@/colada/books'
import { bookFileUrl } from '@/api/files'
import { type Action, actionDetails, ActionName } from '@/types/action/action'
import { useBook } from '@/composables/book/useBook'
import { useBookReadProgress } from '@/composables/book/useBookReadProgress'
import { bookReaderUrl } from '@/api/links'
import type { BookDto } from '@/generated/openapi'
import { useAddToReadListDialog } from '@/composables/book/useAddToReadListDialog'
import { useAddToCollectionDialog } from '@/composables/series/useAddToCollectionDialog'
import { BrowsingContextKey, popBrowsingContext } from '@/functions/browsing-context'
import { useRemoveFromReadList } from '@/composables/book/useRemoveFromReadList'
import { useEditDialog } from '@/composables/entity/useEditDialog'

export function useBookActions(
  book: MaybeRefOrGetter<BookDto>,
  callback: (action: ActionName) => void = () => {},
) {
  const { isAdmin, hasRole } = useCurrentUser()
  const intl = useIntl()
  const { confirm: dialogConfirm } = storeToRefs(useDialogsStore())
  const messagesStore = useMessagesStore()
  const display = useDisplay()
  const { canRead, isEpubReader } = useBook(book)
  const { inProgress } = useBookReadProgress(book)

  // browsing context
  const context = inject(BrowsingContextKey, undefined)
  const contextReadListId = computed(() => {
    const top = popBrowsingContext(toValue(context))?.top
    if (top?.type === 'readList') return top.id
  })

  const actions = computed<Action<ActionName>[]>(() => [
    ...(isAdmin.value && toValue(book).oneshot
      ? [
          {
            title: intl.formatMessage(actionDetails[ActionName.AddToCollection].message),
            action: ActionName.AddToCollection,
            onMouseenter: (event: Event) =>
              (addToCollectionActivator.value = event.currentTarget as Element),
            onClick: () => {
              addToCollection(() => callback(ActionName.AddToCollection))
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage(actionDetails[ActionName.AddToReadList].message),
            action: ActionName.AddToReadList,
            onMouseenter: (event: Event) =>
              (addToReadListActivator.value = event.currentTarget as Element),
            onClick: () => {
              addToReadList(() => callback(ActionName.AddToReadList))
            },
          },
        ]
      : []),
    ...(isAdmin.value && contextReadListId.value
      ? [
          {
            title: intl.formatMessage(actionDetails[ActionName.RemoveFromReadList].message),
            action: ActionName.RemoveFromReadList,
            onMouseenter: (event: Event) =>
              (removeFromReadListActivator.value = event.currentTarget as Element),
            onClick: () => {
              removeFromReadList(() => callback(ActionName.RemoveFromReadList))
            },
          },
        ]
      : []),
    ...(!toValue(book).readProgress?.completed
      ? [
          {
            title: intl.formatMessage(actionDetails[ActionName.MarkRead].message),
            icon: actionDetails[ActionName.MarkRead].icon,
            action: ActionName.MarkRead,
            onClick: () => {
              markRead()
            },
          },
        ]
      : []),
    ...(!!toValue(book).readProgress
      ? [
          {
            title: intl.formatMessage(actionDetails[ActionName.MarkUnread].message),
            icon: actionDetails[ActionName.MarkUnread].icon,
            action: ActionName.MarkUnread,
            onClick: () => {
              markUnread()
            },
          },
        ]
      : []),
    ...(hasRole('FILE_DOWNLOAD')
      ? [
          {
            title: intl.formatMessage(actionDetails[ActionName.Download].message),
            action: ActionName.Download,
            href: bookFileUrl(toValue(book).id),
            onClick: () => {
              callback(ActionName.Download)
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage(actionDetails[ActionName.EditEntity].message),
            icon: actionDetails[ActionName.EditEntity].icon,
            action: ActionName.EditEntity,
            onMouseenter: (event: Event) =>
              (editMetadataActivator.value = event.currentTarget as Element),
            onClick: () => {
              updateBookMetadata(() => callback(ActionName.EditEntity))
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage(actionDetails[ActionName.RefreshMetadata].message),
            action: ActionName.RefreshMetadata,
            onClick: () => {
              refreshMetadata()
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage(actionDetails[ActionName.Analyze].message),

            action: ActionName.Analyze,
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
            action: ActionName.Delete,
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
      icon: actionDetails[ActionName.OpenReader].icon,
      action: ActionName.OpenReader,
      disabled: !canRead.value,
      onClick: () => {
        window.open(bookReaderUrl(toValue(book).id, isEpubReader.value), '_blank')
        callback(ActionName.OpenReader)
      },
    },
    {
      title: intl.formatMessage(actionDetails[ActionName.OpenReaderIncognito].message),
      icon: actionDetails[ActionName.OpenReaderIncognito].icon,
      action: ActionName.OpenReaderIncognito,
      disabled: !canRead.value,
      onClick: () => {
        window.open(bookReaderUrl(toValue(book).id, isEpubReader.value, true), '_blank')
        callback(ActionName.OpenReaderIncognito)
      },
    },
  ])

  //region Add to collection
  const { prepareDialog: showAddToCollectionDialog, activator: addToCollectionActivator } =
    useAddToCollectionDialog()

  function addToCollection(callback: () => void) {
    showAddToCollectionDialog([toValue(book).seriesId], callback)
  }
  //endregion

  //region Add to read list
  const { prepareDialog: showAddToReadListDialog, activator: addToReadListActivator } =
    useAddToReadListDialog()

  function addToReadList(callback: () => void) {
    showAddToReadListDialog([toValue(book)], callback)
  }
  //endregion

  //region Remove from read list
  const { prepareDialog: showRemoveFromReadListDialog, activator: removeFromReadListActivator } =
    useRemoveFromReadList()

  function removeFromReadList(callback: () => void) {
    if (contextReadListId.value)
      void showRemoveFromReadListDialog([toValue(book)], contextReadListId.value, callback)
  }
  //endregion

  //region Update Book metadata
  const { prepareDialog: showEditBookMetadataDialog, activator: editMetadataActivator } =
    useEditDialog()

  function updateBookMetadata(callback: () => void) {
    void showEditBookMetadataDialog([toValue(book)], callback)
  }
  //endregion

  //region Refresh Metadata
  const { mutate: mutateRefreshMetadata } = useRefreshMetadataBook()

  function refreshMetadata() {
    mutateRefreshMetadata(toValue(book).id)
    callback(ActionName.RefreshMetadata)
  }
  //endregion

  //region Analyze
  const { mutate: mutateAnalyze } = useAnalyzeBook()

  function analyzeBook() {
    mutateAnalyze(toValue(book).id)
    callback(ActionName.Analyze)
  }
  //endregion

  //region Mark read
  const { mutate: mutateMarkRead } = useMarkBookRead()

  function markRead() {
    mutateMarkRead(toValue(book).id)
    callback(ActionName.MarkRead)
  }
  //endregion

  //region Mark unread
  const { mutate: mutateMarkUnread } = useMarkBookUnread()

  function markUnread() {
    mutateMarkUnread(toValue(book).id)
    callback(ActionName.MarkUnread)
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
    dialogConfirm.value.callback = (result: DialogResult) => {
      if (result === 'confirm') {
        mutateDelete(toValue(book).id)
          .then(() => {
            messagesStore.messages.push({
              message: intl.formatMessage(
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
            messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
          })
      }

      callback(ActionName.Delete)
    }
  }
  //endregion

  return {
    actions: actions,
  }
}
