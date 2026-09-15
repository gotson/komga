import { createGlobalState } from '@vueuse/core'
import { useTheme } from 'vuetify'
import { useAppStore } from '@/stores/app'

export const useThemeWatcher = createGlobalState(() => {
  const appStore = useAppStore()
  const theme = useTheme()

  function updateTheme(selectedTheme: string) {
    void theme.change(selectedTheme)
  }

  watch(
    () => appStore.theme,
    (selectedTheme) => updateTheme(selectedTheme),
  )

  // trigger an update on startup to get the proper theme loaded
  updateTheme(appStore.theme)
})
