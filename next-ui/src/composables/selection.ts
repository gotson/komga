import { useAppStore } from '@/stores/app'
import { watchImmediate } from '@vueuse/core'
import { useSelectionStore } from '@/stores/selection'
import { useIntl } from 'vue-intl'
import { actionDetails, ActionName } from '@/types/action/action'
import type { BookDto, CollectionDto, ReadListDto, SeriesDto } from '@/generated/openapi'

export function useSelectionContextualActions(
  dataItems: MaybeRefOrGetter<(BookDto | SeriesDto | CollectionDto | ReadListDto)[] | undefined>,
) {
  const intl = useIntl()
  const appStore = useAppStore()
  const selectionStore = useSelectionStore()

  watchImmediate(
    () => appStore.browsingPaging,
    (paging) => {
      selectionStore.contextualActions =
        paging === 'scroll'
          ? [
              {
                text: intl.formatMessage({
                  description: 'Selection bar: select all',
                  defaultMessage: 'Select all',
                  id: 'JHaKYw',
                }),
                icon: 'i-mdi:select-all',
                callback: () => {
                  if (toValue(dataItems)) selectionStore.addMultiple(toValue(dataItems)!)
                },
              },
            ]
          : [
              {
                text: intl.formatMessage({
                  description: 'Selection bar: add page to selection',
                  defaultMessage: 'Add page to selection',
                  id: 'kXuNY8',
                }),
                icon: 'i-mdi:file-document-plus-outline',
                callback: () => {
                  selectionStore.addMultiple(toValue(dataItems)!)
                },
              },
            ]
    },
  )
}

/**
 * Composable that returns a selection action for the given item.
 *
 * @param item
 * @param callback
 */
export function useSelectAction(
  item: MaybeRefOrGetter<BookDto | SeriesDto | CollectionDto | ReadListDto>,
  callback: (action: ActionName) => void = () => {},
) {
  const intl = useIntl()
  const selectionStore = useSelectionStore()

  const selectAction = computed(() => ({
    title: intl.formatMessage(actionDetails[ActionName.Select].message),
    icon: actionDetails[ActionName.Select].icon,
    action: ActionName.Select,
    onClick: () => {
      selectionStore.add(toValue(item))
      callback(ActionName.Select)
    },
  }))

  return { selectAction }
}
