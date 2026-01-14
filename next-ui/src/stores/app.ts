// Utilities
import { defineStore } from 'pinia'
import { useDisplay } from 'vuetify'
import type { PresentationMode } from '@/types/libraries'
import type { PageSize } from '@/types/page'

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
    ],
  },
})
