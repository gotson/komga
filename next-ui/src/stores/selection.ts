import { defineStore } from 'pinia'

export const useSelectionStore = defineStore('selection', () => {
  const route = useRoute()

  const selection = ref<unknown[]>([])

  watch(
    () => route?.path,
    () => {
      selection.value = []
    },
  )

  const isEmpty = computed(() => selection.value.length === 0)
  const count = computed(() => selection.value.length)

  const clear = () => (selection.value = [])

  return { selection, count, isEmpty, clear }
})
