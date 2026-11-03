import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Sort } from '@/types/PageRequest'

export const useSortStore = defineStore(
  'sort',
  () => {
    // Store sort states mapped by viewId
    const savedSortsByView = ref<Record<string, Sort[]>>({})

    function getSavedSorts(viewId: string): Sort[] | undefined {
      return savedSortsByView.value[viewId]
    }

    function saveSortsState(viewId: string, state: Sort[]) {
      savedSortsByView.value[viewId] = JSON.parse(JSON.stringify(state))
    }

    function clearSavedState(viewId: string) {
      delete savedSortsByView.value[viewId]
    }

    function clearAllSavedStates() {
      savedSortsByView.value = {}
    }

    return {
      savedSortsByView,
      getSavedSorts,
      saveSortsState,
      clearSavedState,
      clearAllSavedStates,
    }
  },
  {
    persist: {
      key: 'komga.nextui.sorts',
    },
  },
)
