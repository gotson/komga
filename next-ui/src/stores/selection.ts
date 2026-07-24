import { defineStore } from 'pinia'
import { logger } from '@/services/logtape'
import type { BookDto, ReadListDto, SeriesDto, CollectionDto } from '@/generated/openapi'

type SelectionAction = {
  text: string
  icon: string
  callback: () => void
}

export type SelectionType = BookDto | SeriesDto | CollectionDto | ReadListDto

export const useSelectionStore = defineStore('selection', () => {
  const route = useRoute()

  const selection = ref<Map<string, SelectionType>>(new Map())

  // array bridge for v-data-iterator v-model
  const selectionAsArray = computed({
    get: () => Array.from(selection.value.values()),
    set: (newArray: SelectionType[]) => {
      const newMap = new Map<string, SelectionType>()
      newArray.forEach((item) => newMap.set(item.id, item))
      selection.value = newMap
    },
  })

  watch(
    () => route?.name,
    () => {
      logger.debug('route has changed, clear selection and contextual actions')
      selection.value.clear()
      contextualActions.value = []
    },
  )

  const isEmpty = computed(() => selection.value.size === 0)
  const isNotEmpty = computed(() => selection.value.size > 0)
  const count = computed(() => selection.value.size)

  const isSelected = (item: SelectionType): boolean => selection.value.has(item.id)

  const add = (item: SelectionType) => selection.value.set(item.id, item)
  const addMultiple = (items: SelectionType[]) =>
    items.forEach((item) => selection.value.set(item.id, item))
  const remove = (item: SelectionType) => selection.value.delete(item.id)
  const toggle = (item: SelectionType) => {
    if (selection.value.has(item.id)) {
      selection.value.delete(item.id)
    } else {
      selection.value.set(item.id, item)
    }
  }
  const clear = () => selection.value.clear()

  const contextualActions = ref<SelectionAction[]>([])

  return {
    selection,
    selectionAsArray,
    contextualActions,
    count,
    isEmpty,
    isNotEmpty,
    isSelected,
    add,
    addMultiple,
    remove,
    toggle,
    clear,
  }
})
