import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { FilterStateMap } from '@/composables/filter'

export const useFilterStore = defineStore(
  'filter',
  () => {
    // Store filter states mapped by viewId
    const savedFiltersByView = ref<Record<string, Partial<FilterStateMap>>>({})

    function getSavedFilters(viewId: string): Partial<FilterStateMap> | undefined {
      return savedFiltersByView.value[viewId]
    }

    function saveFilterState(viewId: string, state: Partial<FilterStateMap>) {
      savedFiltersByView.value[viewId] = JSON.parse(JSON.stringify(state))
    }

    function clearSavedState(viewId: string) {
      delete savedFiltersByView.value[viewId]
    }

    function clearAllSavedStates() {
      savedFiltersByView.value = {}
    }

    return {
      savedFiltersByView,
      getSavedFilters,
      saveFilterState,
      clearSavedState,
      clearAllSavedStates,
    }
  },
  {
    persist: {
      key: 'komga.nextui.filters',
    },
  },
)
