import { useAppStore } from '@/stores/app'
import { watchImmediate } from '@vueuse/core'
import { useSelectionStore } from '@/stores/selection'
import { pushIfAbsent } from '@/functions/array'
import { useIntl } from 'vue-intl'
import { actionDetails, ActionName } from '@/types/action/action'
import type { components } from '@/generated/openapi/komga'

export function useSelectionContextualActions(
  dataItems: MaybeRefOrGetter<
    | (
        | components['schemas']['BookDto']
        | components['schemas']['SeriesDto']
        | components['schemas']['CollectionDto']
        | components['schemas']['ReadListDto']
      )[]
    | undefined
  >,
  isSame: (
    existing: unknown,
    toAdd:
      | components['schemas']['BookDto']
      | components['schemas']['SeriesDto']
      | components['schemas']['CollectionDto']
      | components['schemas']['ReadListDto'],
  ) => boolean,
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
                  if (toValue(dataItems)) selectionStore.selection = toValue(dataItems)!
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
                  pushIfAbsent(selectionStore.selection, toValue(dataItems), isSame)
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
  item: MaybeRefOrGetter<
    | components['schemas']['BookDto']
    | components['schemas']['SeriesDto']
    | components['schemas']['CollectionDto']
    | components['schemas']['ReadListDto']
  >,
  callback: (action: ActionName) => void = () => {},
) {
  const intl = useIntl()
  const selectionStore = useSelectionStore()

  const selectAction = computed(() => ({
    title: intl.formatMessage(actionDetails[ActionName.SELECT].message),
    icon: actionDetails[ActionName.SELECT].icon,
    action: ActionName.SELECT,
    onClick: () => {
      selectionStore.selection.push(toValue(item))
      callback(ActionName.SELECT)
    },
  }))

  return { selectAction }
}
