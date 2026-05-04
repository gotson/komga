// Utilities
import { defineStore } from 'pinia'
import { useDisplay } from 'vuetify'
import type { PresentationMode } from '@/types/libraries'
import type { PageSize, Paging } from '@/types/page'
import type { Sort } from '@/types/PageRequest'

interface AppState {
  drawer: boolean
  theme: string
  rememberMe: boolean
  importBooksPath: string
  browsingPageSize: PageSize
  browsingPaging: Paging
  presentationMode: Record<string, PresentationMode>
  sortActive: Record<string, Sort[]>
  gridCardWidth: number
  reorderLibraries: boolean
}

export const useAppStore = defineStore('app', {
  state: (): AppState => ({
    // persisted
    drawer: !useDisplay().mobile.value.valueOf(),
    theme: 'system',
    rememberMe: false,
    importBooksPath: '',
    browsingPageSize: 20,
    browsingPaging: 'scroll',
    /**
     * Store the presentation mode per view.
     * Use the getter to ensure a default value is always set.
     */
    presentationMode: {},
    /**
     * Store the sort order per view.
     * Use the getter to ensure a default value is always set.
     */
    sortActive: {},
    gridCardWidth: 150,
    // transient
    reorderLibraries: false,
  }),
  getters: {
    isBrowsingPaged(state) {
      return state.browsingPaging === 'paged'
    },
    isBrowsingScroll(state) {
      return state.browsingPaging === 'scroll'
    },
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
      'browsingPaging',
      'presentationMode',
      'sortActive',
      'gridCardWidth',
    ],
  },
})
