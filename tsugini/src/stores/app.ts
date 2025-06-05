import { defineStore, acceptHMRUpdate } from 'pinia'
import { useQuasar } from 'quasar'

export const useAppStore = defineStore('app', {
  state: () => ({
    loginRememberMe: false,
    drawer: !useQuasar().platform.is.mobile.valueOf(),
    theme: 'auto' as 'auto' | boolean,
  }),
  persist: true,
})

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useAppStore, import.meta.hot))
}
