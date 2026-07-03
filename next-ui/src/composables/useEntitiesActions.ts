import { useCurrentUser } from '@/colada/users'
import { useIntl } from 'vue-intl'
import { storeToRefs } from 'pinia'
import { useDialogsStore } from '@/stores/dialogs'
import { useDisplay } from 'vuetify/framework'
import EntitiesDeletionWarning from '@/components/entities/DeletionWarning.vue'
import {
  useAnalyzeBook,
  useDeleteBook,
  useMarkBookRead,
  useMarkBookUnread,
  useRefreshMetadataBook,
} from '@/colada/books'
import { type Action, actionDetails, ActionName } from '@/types/action/action'
import { getCommonActions } from '@/functions/selection'
import { isBook, isSeries, resolveEntityKind } from '@/functions/entity'
import {
  useAnalyzeSeries,
  useDeleteSeries,
  useMarkSeriesRead,
  useMarkSeriesUnread,
  useRefreshMetadataSeries,
} from '@/colada/series'
import type { BookDto, CollectionDto, ReadListDto, SeriesDto } from '@/generated/openapi'

export function useEntitiesActions(
  entities: MaybeRefOrGetter<(BookDto | SeriesDto | CollectionDto | ReadListDto)[]>,
  callback: (action: ActionName) => void = () => {},
) {
  const { isAdmin } = useCurrentUser()
  const intl = useIntl()
  const { confirm: dialogConfirm } = storeToRefs(useDialogsStore())
  const display = useDisplay()

  const entityKinds = computed(() => Array.from(new Set(toValue(entities).map(resolveEntityKind))))
  const commonActions = computed(() => getCommonActions(entityKinds.value))

  //TODO: implement remaining actions
  const actionsSpecifics: Partial<Record<ActionName, object>> = {
    [ActionName.AddToCollection]: {
      disabled: true,
    },
    [ActionName.AddToReadList]: {
      disabled: true,
    },
    [ActionName.EditBook]: {
      disabled: true,
    },
    [ActionName.EditSeries]: {
      disabled: true,
    },
    [ActionName.MarkRead]: {
      onClick: () => markRead(),
    },
    [ActionName.MarkUnread]: {
      onClick: () => markUnread(),
    },
    [ActionName.Analyze]: {
      onClick: () => analyze(),
    },
    [ActionName.RefreshMetadata]: {
      onClick: () => refreshMetadata(),
    },
    [ActionName.Delete]: {
      onMouseenter: (event: Event) =>
        (dialogConfirm.value.activator = event.currentTarget as Element),
      onClick: () => {
        deleteAll()
      },
    },
  }
  const nonAdmin = [ActionName.MarkRead, ActionName.MarkUnread] as ActionName[]

  const actions = computed<Action<ActionName>[]>(() =>
    commonActions.value
      .map((action) => ({
        title: intl.formatMessage(actionDetails[action].message),
        icon: actionDetails[action].icon,
        action: action,
        ...actionsSpecifics[action],
      }))
      .filter((action) => (isAdmin.value ? true : nonAdmin.includes(action.action))),
  )

  //region Update Series metadata
  // const { prepareDialog: showEditBookMetadataDialog, activator: editMetadataActivator } =
  //   useEditBookMetadataDialog()
  //
  // function updateBookMetadata() {
  //   showEditBookMetadataDialog(toValue(book))
  // }
  //endregion

  //region Refresh Metadata
  function refreshMetadata() {
    toValue(entities).forEach((item) => {
      if (isBook(item)) useRefreshMetadataBook().mutate(item.id)
      if (isSeries(item)) useRefreshMetadataSeries().mutate(item.id)
    })

    callback(ActionName.RefreshMetadata)
  }
  //endregion

  //region Analyze
  function analyze() {
    toValue(entities).forEach((item) => {
      if (isBook(item)) useAnalyzeBook().mutate(item.id)
      if (isSeries(item)) useAnalyzeSeries().mutate(item.id)
    })

    callback(ActionName.Analyze)
  }
  //endregion

  //region Mark read
  function markRead() {
    toValue(entities).forEach((item) => {
      if (isBook(item)) useMarkBookRead().mutate(item.id)
      if (isSeries(item)) useMarkSeriesRead().mutate(item.id)
    })

    callback(ActionName.MarkRead)
  }
  //endregion

  //region Mark unread
  function markUnread() {
    toValue(entities).forEach((item) => {
      if (isBook(item)) useMarkBookUnread().mutate(item.id)
      if (isSeries(item)) useMarkSeriesUnread().mutate(item.id)
    })

    callback(ActionName.MarkUnread)
  }
  //endregion

  //region Delete
  function deleteAll() {
    dialogConfirm.value.dialogProps = {
      title: intl.formatMessage({
        description: 'Multiple entities deletion dialog: title',
        defaultMessage: 'Delete',
        id: 'aU1qcP',
      }),
      subtitle: intl.formatMessage(
        {
          description: 'Multiple entities deletion dialog: subtitle',
          defaultMessage: '{count} items',
          id: 'Ss08kC',
        },
        { count: toValue(entities).length },
      ),
      maxWidth: 600,
      mode: 'checkbox',
      color: 'error',
      okText: intl.formatMessage({
        description: 'Multiple entities deletion dialog: confirm button',
        defaultMessage: 'Delete',
        id: '8kylzj',
      }),
      closeOnSave: true,
      fullscreen: display.xs.value,
    }
    dialogConfirm.value.slotWarning = {
      component: markRaw(EntitiesDeletionWarning),
      props: {},
    }
    dialogConfirm.value.callback = () => {
      toValue(entities).forEach((item) => {
        if (isBook(item)) useDeleteBook().mutate(item.id)
        if (isSeries(item)) useDeleteSeries().mutate(item.id)
      })

      callback(ActionName.Delete)
    }
  }
  //endregion

  return {
    actions: actions,
  }
}
