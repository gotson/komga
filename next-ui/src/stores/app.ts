// Utilities
import { defineStore } from 'pinia'
import { useDisplay } from 'vuetify'

export const useAppStore = defineStore('app', {
  state: () => ({
    drawer: !useDisplay().mobile.value.valueOf(),
    theme: 'system',
    rememberMe: false,
  }),
  persist: true,
})
