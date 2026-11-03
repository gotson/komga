import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { LibrarySubType } from '@/types/librarySubRoute'

export const useLibraryViewStore = defineStore(
  'libraryView',
  () => {
    // Store preferred view mapped by viewId
    const savedStateByView = ref<Record<string, LibrarySubType>>({})

    function getSavedState(viewId: string): LibrarySubType | undefined {
      return savedStateByView.value[viewId]
    }

    function saveState(viewId: string, state: LibrarySubType) {
      savedStateByView.value[viewId] = state
    }

    function clearSavedState(viewId: string) {
      delete savedStateByView.value[viewId]
    }

    function clearAllSavedStates() {
      savedStateByView.value = {}
    }

    return {
      savedFiltersByView: savedStateByView,
      getSavedState,
      saveState,
      clearSavedState,
      clearAllSavedStates,
    }
  },
  {
    persist: {
      key: 'komga.nextui.library-view',
    },
  },
)
