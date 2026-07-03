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
            title: intl.formatMessage(actionDetails[ActionName.AddToCollection].message),
            disabled: true, //TODO: implement
            action: ActionName.AddToCollection,
            onClick: () => {
              todo()
              callback(ActionName.AddToCollection)
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage(actionDetails[ActionName.AddToReadList].message),
            disabled: true, //TODO: implement
            action: ActionName.AddToReadList,
            onClick: () => {
              todo()
              callback(ActionName.AddToReadList)
            },
          },
        ]
      : []),
    ...(toValue(series).booksReadCount === toValue(series).booksCount
      ? []
      : [
          {
            title: intl.formatMessage(actionDetails[ActionName.MarkRead].message),
            icon: actionDetails[ActionName.MarkRead].icon,
            action: ActionName.MarkRead,
            onClick: () => {
              markRead()
            },
          },
        ]),
    ...(toValue(series).booksUnreadCount === toValue(series).booksCount
      ? []
      : [
          {
            title: intl.formatMessage(actionDetails[ActionName.MarkUnread].message),
            icon: actionDetails[ActionName.MarkUnread].icon,
            action: ActionName.MarkUnread,
            onClick: () => {
              markUnread()
            },
          },
        ]),
    ...(hasRole('FILE_DOWNLOAD')
      ? [
          {
            title: intl.formatMessage(actionDetails[ActionName.Download].message),
            action: ActionName.Download,
            href: seriesFileUrl(toValue(series).id),
            onClick: () => {
              callback(ActionName.Download)
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage(actionDetails[ActionName.EditSeries].message),
            icon: actionDetails[ActionName.EditSeries].icon,
            action: ActionName.EditSeries,
            onMouseenter: (event: Event) =>
              (editMetadataActivator.value = event.currentTarget as Element),
            onClick: () => {
              updateSeriesMetadata(() => callback(ActionName.EditSeries))
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
            action: ActionName.Delete,
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
      icon: actionDetails[ActionName.OpenReader].icon,
      action: ActionName.OpenReader,
      disabled: !canRead.value,
      onClick: () => {
        void readFirstBook()
        callback(ActionName.OpenReader)
      },
    },
    {
      title: intl.formatMessage(actionDetails[ActionName.OpenReaderIncognito].message),
      icon: actionDetails[ActionName.OpenReaderIncognito].icon,
      action: ActionName.OpenReaderIncognito,
      disabled: !canRead.value,
      onClick: () => {
        void readFirstBook(true)
        callback(ActionName.OpenReaderIncognito)
      },
    },
  ])
  //region Update Series metadata
  const { prepareDialog: showEditSeriesMetadataDialog, activator: editMetadataActivator } =
    useEditSeriesMetadataDialog()

  function updateSeriesMetadata(callback: () => void) {
    showEditSeriesMetadataDialog(toValue(series), callback)
  }
  //endregion

  //region Refresh Metadata
  const { mutate: mutateRefreshMetadata } = useRefreshMetadataSeries()

  function refreshMetadata() {
    mutateRefreshMetadata(toValue(series).id)
    callback(ActionName.RefreshMetadata)
  }
  //endregion

  //region Analyze
  const { mutate: mutateAnalyze } = useAnalyzeSeries()

  function analyzeSeries() {
    mutateAnalyze(toValue(series).id)
    callback(ActionName.Analyze)
  }
  //endregion

  //TODO: do :)
  function todo() {}

  //region Mark read
  const { mutate: mutateMarkRead } = useMarkSeriesRead()

  function markRead() {
    mutateMarkRead(toValue(series).id)
    callback(ActionName.MarkRead)
  }
  //endregion

  //region Mark unread
  const { mutate: mutateMarkUnread } = useMarkSeriesUnread()

  function markUnread() {
    mutateMarkUnread(toValue(series).id)
    callback(ActionName.MarkUnread)
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
      callback(ActionName.Delete)
    }
  }
  //endregion

  return {
    actions: actions,
  }
}
