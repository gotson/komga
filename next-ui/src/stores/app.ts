// Utilities
import { defineStore } from 'pinia'
import { useDisplay } from 'vuetify'

export const useAppStore = defineStore('app', {
  state: () => ({
    // persisted
    drawer: !useDisplay().mobile.value.valueOf(),
    theme: 'system',
    rememberMe: false,
    importBooksPath: '',
    // transient
    reorderLibraries: false,
  }),
  persist: {
    key: 'komga.nextui.app',
    // explicitly state which keys are stored
    pick: ['drawer', 'theme', 'rememberMe', 'importBooksPath'],
  },
})
