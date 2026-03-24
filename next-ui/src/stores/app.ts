// Utilities
import { defineStore } from 'pinia'
import { useDisplay } from 'vuetify'
import type { PresentationMode } from '@/types/libraries'
import type { PageSize } from '@/types/page'
import type { Sort } from '@/types/PageRequest'

export const useAppStore = defineStore('app', {
  state: () => ({
    // persisted
    drawer: !useDisplay().mobile.value.valueOf(),
    theme: 'system',
    rememberMe: false,
    importBooksPath: '',
    browsingPageSize: 20 as PageSize,
    /**
     * Store the presentation mode per view.
     * Use the getter to ensure a default value is always set.
     */
    presentationMode: {} as Record<string, PresentationMode>,
    /**
     * Store the sort order per view.
     * Use the getter to ensure a default value is always set.
     */
    sortActive: {} as Record<string, Sort[]>,
    gridCardWidth: 150,
    // transient
    reorderLibraries: false,
  }),
  getters: {
    getPresentationMode: (state) => (key: string, defaultValue: PresentationMode) => {
      return computed({
        get: () => state.presentationMode[key] ?? (state.presentationMode[key] = defaultValue),
        set: (value) => {
          state.presentationMode[key] = value
        },
      })
    },
    getSortActive: (state) => (key: string, defaultValue: Sort[]) => {
      return computed({
        get: () => state.sortActive[key] ?? (state.sortActive[key] = defaultValue),
        set: (value) => {
          state.sortActive[key] = value
        },
      })
    },
  },
  persist: {
    key: 'komga.nextui.app',
    // explicitly state which keys are stored
    pick: [
      'drawer',
      'theme',
      'rememberMe',
      'importBooksPath',
      'browsingPageSize',
      'presentationMode',
      'gridCardWidth',
    ],
  },
})
