import { defineStore } from 'pinia'
import { logger } from '@/services/logtape'
import type { components } from '@/generated/openapi/komga'

type SelectionAction = {
  text: string
  icon: string
  callback: () => void
}

export const useSelectionStore = defineStore('selection', () => {
  const route = useRoute()

  const selection = ref<
    (
      | components['schemas']['BookDto']
      | components['schemas']['SeriesDto']
      | components['schemas']['CollectionDto']
      | components['schemas']['ReadListDto']
    )[]
  >([])

  watch(
    () => route?.path,
    () => {
      logger.debug('route has changed, clear selection and contextual actions')
      selection.value = []
      contextualActions.value = []
    },
  )

  const isEmpty = computed(() => selection.value.length === 0)
  const isNotEmpty = computed(() => selection.value.length > 0)
  const count = computed(() => selection.value.length)

  const clear = () => (selection.value = [])

  const contextualActions = ref<SelectionAction[]>([])

  return {
    selection,
    contextualActions,
    count,
    isEmpty,
    isNotEmpty,
    clear,
  }
})
