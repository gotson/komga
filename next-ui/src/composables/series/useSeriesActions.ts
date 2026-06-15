import type { components } from '@/generated/openapi/komga'
import type { ErrorCause } from '@/api/komga-client'
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
import { SeriesAction } from '@/types/series'
import SeriesDeletionWarning from '@/components/series/DeletionWarning.vue'
import { useEditSeriesMetadataDialog } from '@/composables/series/useEditSeriesMetadataDialog'
import { UserRoles } from '@/types/UserRoles'
import { seriesFileUrl } from '@/api/files'
import type { Action } from '@/types/action'
import { useSeriesBooks } from '@/composables/series/useSeriesBooks'
import { useSeries } from '@/composables/series/useSeries'

export function useSeriesActions(
  series: MaybeRefOrGetter<components['schemas']['SeriesDto']>,
  callback: (action: SeriesAction) => void = () => {},
) {
  const { isAdmin, hasRole } = useCurrentUser()
  const intl = useIntl()
  const { confirm: dialogConfirm } = storeToRefs(useDialogsStore())
  const messagesStore = useMessagesStore()
  const display = useDisplay()
  const { readFirstBook } = useSeriesBooks(() => toValue(series).id)
  const { canRead, inProgress } = useSeries(series)

  const actions = computed<Action<SeriesAction>[]>(() => [
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage({
              description: 'Series menu: add to collection',
              defaultMessage: 'Add to collection',
              id: 'BAKokv',
            }),
            action: SeriesAction.ADD_TO_COLLECTION,
            onClick: () => {
              todo()
              callback(SeriesAction.ADD_TO_COLLECTION)
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage({
              description: 'Series menu: add to read list',
              defaultMessage: 'Add to read list',
              id: '9eEylZ',
            }),
            action: SeriesAction.ADD_TO_READLIST,
            onClick: () => {
              todo()
              callback(SeriesAction.ADD_TO_READLIST)
            },
          },
        ]
      : []),
    ...(toValue(series).booksReadCount === toValue(series).booksCount
      ? []
      : [
          {
            title: intl.formatMessage({
              description: 'Series menu: mark as read',
              defaultMessage: 'Mark as read',
              id: 'SZWIZ7',
            }),
            icon: 'i-mdi:book-check-outline',
            action: SeriesAction.MARK_READ,
            onClick: () => {
              markRead()
              callback(SeriesAction.MARK_READ)
            },
          },
        ]),
    ...(toValue(series).booksUnreadCount === toValue(series).booksCount
      ? []
      : [
          {
            title: intl.formatMessage({
              description: 'Series menu: mark as unread',
              defaultMessage: 'Mark as unread',
              id: 'JL33DG',
            }),
            icon: 'i-mdi:book-remove-outline',
            action: SeriesAction.MARK_UNREAD,
            onClick: () => {
              markUnread()
              callback(SeriesAction.MARK_UNREAD)
            },
          },
        ]),
    ...(hasRole(UserRoles.FILE_DOWNLOAD)
      ? [
          {
            title: intl.formatMessage({
              description: 'Series menu: download',
              defaultMessage: 'Download',
              id: 'jn8Lib',
            }),
            action: SeriesAction.DOWNLOAD,
            href: seriesFileUrl(toValue(series).id),
            onClick: () => {
              callback(SeriesAction.DOWNLOAD)
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage({
              description: 'Series menu: manage > edit metadata',
              defaultMessage: 'Edit metadata',
              id: 'O839kY',
            }),
            icon: 'i-mdi:pencil',
            action: SeriesAction.EDIT_METADATA,
            onMouseenter: (event: Event) =>
              (editMetadataActivator.value = event.currentTarget as Element),
            onClick: () => {
              updateSeriesMetadata()
              callback(SeriesAction.EDIT_METADATA)
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage({
              description: 'Series menu: manage > refresh metadata',
              defaultMessage: 'Refresh metadata',
              id: 'JFtKtC',
            }),
            action: SeriesAction.REFRESH_METADATA,
            onClick: () => {
              refreshMetadata()
              callback(SeriesAction.REFRESH_METADATA)
            },
          },
        ]
      : []),
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage({
              description: 'Series menu: manage > analyze',
              defaultMessage: 'Analyze',
              id: 'AI60X8',
            }),
            action: SeriesAction.ANALYZE,
            onClick: () => {
              analyzeSeries()
              callback(SeriesAction.ANALYZE)
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
            action: SeriesAction.DELETE_FILES,
            onMouseenter: (event: Event) =>
              (dialogConfirm.value.activator = event.currentTarget as Element),
            onClick: () => {
              deleteSeries()
              callback(SeriesAction.DELETE_FILES)
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
      icon: 'i-mdi:play',
      action: SeriesAction.OPEN_READER,
      disabled: !canRead.value,
      onClick: () => {
        void readFirstBook()
        callback(SeriesAction.OPEN_READER)
      },
    },
    {
      title: intl.formatMessage({
        description: 'Series view: read incognito button: tooltip',
        defaultMessage: 'Private reading session',
        id: 'DLUIbm',
      }),
      icon: 'i-mdi:incognito',
      action: SeriesAction.OPEN_READER_INCOGNITO,
      disabled: !canRead.value,
      onClick: () => {
        void readFirstBook(true)
        callback(SeriesAction.OPEN_READER_INCOGNITO)
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
  }
  //endregion

  //region Analyze
  const { mutate: mutateAnalyze } = useAnalyzeSeries()

  function analyzeSeries() {
    mutateAnalyze(toValue(series).id)
  }
  //endregion

  function todo() {}

  //region Mark read
  const { mutate: mutateMarkRead } = useMarkSeriesRead()

  function markRead() {
    mutateMarkRead(toValue(series).id)
  }
  //endregion

  //region Mark unread
  const { mutate: mutateMarkUnread } = useMarkSeriesUnread()

  function markUnread() {
    mutateMarkUnread(toValue(series).id)
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
            text: intl.formatMessage(
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
