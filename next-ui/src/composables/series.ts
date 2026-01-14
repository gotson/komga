import type { components } from '@/generated/openapi/komga'
import EditMetadata from '@/components/series/form/EditMetadata.vue'
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
  useUpdateSeriesMetadata,
} from '@/colada/series'
import { useCurrentUser } from '@/colada/users'
import { SeriesAction } from '@/types/series'
import SeriesDeletionWarning from '@/components/series/DeletionWarning.vue'

export function useEditSeriesMetadataDialog() {
  const { confirmEdit: dialogConfirmEdit } = storeToRefs(useDialogsStore())
  const intl = useIntl()
  const display = useDisplay()
  const messagesStore = useMessagesStore()
  const { mutateAsync: mutateUpdateSeriesMetadata } = useUpdateSeriesMetadata()

  const showDialog = (series: components['schemas']['SeriesDto']) => {
    dialogConfirmEdit.value.dialogProps = {
      title: intl.formatMessage({
        description: 'Edit series metadata dialog title',
        defaultMessage: 'Edit series metadata',
        id: '1bxWGd',
      }),
      subtitle: series.metadata.title,
      maxWidth: 600,
      okText: 'Save',
      cardTextClass: 'px-0',
      closeOnSave: false,
      scrollable: true,
      fullscreen: display.xs.value,
    }
    dialogConfirmEdit.value.slot = {
      component: markRaw(EditMetadata),
    }
    dialogConfirmEdit.value.record = series.metadata
    dialogConfirmEdit.value.callback = (
      hideDialog: () => void,
      setLoading: (isLoading: boolean) => void,
    ) => {
      setLoading(true)

      const updatedMetadata = dialogConfirmEdit.value
        .record as components['schemas']['SeriesMetadataDto']

      mutateUpdateSeriesMetadata({ seriesId: series.id, metadata: updatedMetadata })
        .then(() => {
          hideDialog()
          messagesStore.messages.push({
            text: intl.formatMessage(
              {
                description: 'Snackbar notification shown upon successful series metadata update',
                defaultMessage: 'Series metadata updated: {series}',
                id: 'gEBeQv',
              },
              {
                series: updatedMetadata.title,
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
          setLoading(false)
        })
    }
  }

  const activatorRef = computed({
    get: () => dialogConfirmEdit.value.activator,
    set: (val) => (dialogConfirmEdit.value.activator = val),
  })

  return {
    showDialog: showDialog,
    activator: activatorRef,
  }
}

export function useSeriesActions(
  series: components['schemas']['SeriesDto'],
  callback: (action: SeriesAction) => void = () => {},
) {
  const { isAdmin } = useCurrentUser()
  const intl = useIntl()
  const { confirm: dialogConfirm } = storeToRefs(useDialogsStore())
  const messagesStore = useMessagesStore()
  const display = useDisplay()

  const actions = computed(() => [
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
    ...(series.booksReadCount === series.booksCount
      ? []
      : [
          {
            title: intl.formatMessage({
              description: 'Series menu: mark as read',
              defaultMessage: 'Mark as read',
              id: 'SZWIZ7',
            }),
            action: SeriesAction.MARK_READ,
            onClick: () => {
              markRead()
              callback(SeriesAction.MARK_READ)
            },
          },
        ]),
    ...(series.booksUnreadCount === series.booksCount
      ? []
      : [
          {
            title: intl.formatMessage({
              description: 'Series menu: mark as unread',
              defaultMessage: 'Mark as unread',
              id: 'JL33DG',
            }),
            action: SeriesAction.MARK_UNREAD,
            onClick: () => {
              markUnread()
              callback(SeriesAction.MARK_UNREAD)
            },
          },
        ]),
  ])

  const manageActions = computed(() => [
    ...(isAdmin.value
      ? [
          {
            title: intl.formatMessage({
              description: 'Series menu: manage > edit metadata',
              defaultMessage: 'Edit metadata',
              id: 'O839kY',
            }),
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
  ])

  //region Update Series metadata
  const { showDialog: showEditSeriesMetadataDialog, activator: editMetadataActivator } =
    useEditSeriesMetadataDialog()

  function updateSeriesMetadata() {
    showEditSeriesMetadataDialog(series)
  }
  //endregion

  //region Refresh Metadata
  const { mutate: mutateRefreshMetadata } = useRefreshMetadataSeries()

  function refreshMetadata() {
    mutateRefreshMetadata(series.id)
  }
  //endregion

  //region Analyze
  const { mutate: mutateAnalyze } = useAnalyzeSeries()

  function analyzeSeries() {
    mutateAnalyze(series.id)
  }
  //endregion

  function todo() {}

  //region Mark read
  const { mutate: mutateMarkRead } = useMarkSeriesRead()

  function markRead() {
    mutateMarkRead(series.id)
  }
  //endregion

  //region Mark unread
  const { mutate: mutateMarkUnread } = useMarkSeriesUnread()

  function markUnread() {
    mutateMarkUnread(series.id)
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
      subtitle: series.metadata.title,
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
      mutateDelete(series.id)
        .then(() => {
          messagesStore.messages.push({
            text: intl.formatMessage(
              {
                description: 'Snackbar notification shown upon successful series files deletion',
                defaultMessage: 'Series files deleted: {series}',
                id: 'aSDxrt',
              },
              {
                series: series.metadata.title,
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
