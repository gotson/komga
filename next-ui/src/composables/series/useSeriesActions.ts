import { commonMessages } from '@/utils/i18n/common-messages'
import { useDialogsStore } from '@/stores/dialogs'
import { storeToRefs } from 'pinia'
import { useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify'
import { useMessagesStore } from '@/stores/messages'
import {
  useAnalyzeSeries,
  useDeleteSeries,
  useMarkSeriesRead,
  useMarkSeriesUnread,
  useRefreshMetadataSeries,
} from '@/colada/series'
import { useCurrentUser } from '@/colada/users'
import SeriesDeletionWarning from '@/components/series/DeletionWarning.vue'
import { useEditSeriesMetadataDialog } from '@/composables/series/useEditSeriesMetadataDialog'
import { UserRoles } from '@/types/UserRoles'
import { seriesFileUrl } from '@/api/files'
import { type Action, actionDetails, ActionName } from '@/types/action/action'
import { useSeriesBooks } from '@/composables/series/useSeriesBooks'
import { useSeries } from '@/composables/series/useSeries'
import type { SeriesDto } from '@/generated/openapi'

export function useSeriesActions(
  series: MaybeRefOrGetter<SeriesDto>,
  callback: (action: ActionName) => void = () => {},
) {
  const { isAdmin, hasRole } = useCurrentUser()
  const intl = useIntl()
  const { confirm: dialogConfirm } = storeToRefs(useDialogsStore())
  const messagesStore = useMessagesStore()
  const display = useDisplay()
  const { readFirstBook } = useSeriesBooks(() => toValue(series).id)
  const { canRead, inProgress } = useSeries(series)

  const actions = computed<Action<ActionName>[]>(() => [
    ...(isAdmin.value
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
    ...(toValue(series).booksReadCount === toValue(series).booksCount
      ? []
      : [
          {
            title: intl.formatMessage(actionDetails[ActionName.MARK_READ].message),
            icon: actionDetails[ActionName.MARK_READ].icon,
            action: ActionName.MARK_READ,
            onClick: () => {
              markRead()
            },
          },
        ]),
    ...(toValue(series).booksUnreadCount === toValue(series).booksCount
      ? []
      : [
          {
            title: intl.formatMessage(actionDetails[ActionName.MARK_UNREAD].message),
            icon: actionDetails[ActionName.MARK_UNREAD].icon,
            action: ActionName.MARK_UNREAD,
            onClick: () => {
              markUnread()
            },
          },
        ]),
    ...(hasRole(UserRoles.FILE_DOWNLOAD)
      ? [
          {
            title: intl.formatMessage(actionDetails[ActionName.DOWNLOAD].message),
            action: ActionName.DOWNLOAD,
            href: seriesFileUrl(toValue(series).id),
            onClick: () => {
              callback(ActionName.DOWNLOAD)
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage(actionDetails[ActionName.EDIT_SERIES].message),
            icon: actionDetails[ActionName.EDIT_SERIES].icon,
            action: ActionName.EDIT_SERIES,
            onMouseenter: (event: Event) =>
              (editMetadataActivator.value = event.currentTarget as Element),
            onClick: () => {
              updateSeriesMetadata()
              callback(ActionName.EDIT_SERIES) //TODO: move callback after validation
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
              analyzeSeries()
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage({
              description: 'Series menu: manage > delete file',
              defaultMessage: 'Delete files',
              id: 'J+H9cC',
            }),
            action: ActionName.DELETE,
            onMouseenter: (event: Event) =>
              (dialogConfirm.value.activator = event.currentTarget as Element),
            onClick: () => {
              deleteSeries()
            },
          },
        ]
      : []),
    {
      title: inProgress.value
        ? intl.formatMessage({
            description: 'Series menu: resume reading button',
            defaultMessage: 'Resume',
            id: 'wXGljO',
          })
        : intl.formatMessage({
            description: 'Series menu: read button',
            defaultMessage: 'Read',
            id: 'Y7Y2T0',
          }),
      icon: actionDetails[ActionName.OPEN_READER].icon,
      action: ActionName.OPEN_READER,
      disabled: !canRead.value,
      onClick: () => {
        void readFirstBook()
        callback(ActionName.OPEN_READER)
      },
    },
    {
      title: intl.formatMessage(actionDetails[ActionName.OPEN_READER_INCOGNITO].message),
      icon: actionDetails[ActionName.OPEN_READER_INCOGNITO].icon,
      action: ActionName.OPEN_READER_INCOGNITO,
      disabled: !canRead.value,
      onClick: () => {
        void readFirstBook(true)
        callback(ActionName.OPEN_READER_INCOGNITO)
      },
    },
  ])
  //region Update Series metadata
  const { prepareDialog: showEditSeriesMetadataDialog, activator: editMetadataActivator } =
    useEditSeriesMetadataDialog()

  function updateSeriesMetadata() {
    showEditSeriesMetadataDialog(toValue(series))
  }
  //endregion

  //region Refresh Metadata
  const { mutate: mutateRefreshMetadata } = useRefreshMetadataSeries()

  function refreshMetadata() {
    mutateRefreshMetadata(toValue(series).id)
    callback(ActionName.REFRESH_METADATA)
  }
  //endregion

  //region Analyze
  const { mutate: mutateAnalyze } = useAnalyzeSeries()

  function analyzeSeries() {
    mutateAnalyze(toValue(series).id)
    callback(ActionName.ANALYZE)
  }
  //endregion

  //TODO: do :)
  function todo() {}

  //region Mark read
  const { mutate: mutateMarkRead } = useMarkSeriesRead()

  function markRead() {
    mutateMarkRead(toValue(series).id)
    callback(ActionName.MARK_READ)
  }
  //endregion

  //region Mark unread
  const { mutate: mutateMarkUnread } = useMarkSeriesUnread()

  function markUnread() {
    mutateMarkUnread(toValue(series).id)
    callback(ActionName.MARK_UNREAD)
  }
  //endregion

  //region Delete
  const { mutateAsync: mutateDelete } = useDeleteSeries()

  function deleteSeries() {
    dialogConfirm.value.dialogProps = {
      title: intl.formatMessage({
        description: 'Series delete dialog: title',
        defaultMessage: 'Delete series files',
        id: 'Xxu514',
      }),
      subtitle: toValue(series).metadata.title,
      maxWidth: 600,
      mode: 'checkbox',
      color: 'error',
      okText: intl.formatMessage({
        description: 'Series delete dialog: confirm button',
        defaultMessage: 'Delete files',
        id: 'DfakWW',
      }),
      closeOnSave: true,
      fullscreen: display.xs.value,
    }
    dialogConfirm.value.slotWarning = {
      component: markRaw(SeriesDeletionWarning),
      props: {},
    }
    dialogConfirm.value.callback = () => {
      mutateDelete(toValue(series).id)
        .then(() => {
          messagesStore.messages.push({
            message: intl.formatMessage(
              {
                description: 'Snackbar notification shown upon successful series files deletion',
                defaultMessage: 'Series files deleted: {series}',
                id: 'aSDxrt',
              },
              {
                series: toValue(series).metadata.title,
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
