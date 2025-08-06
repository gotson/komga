import { useTheme } from 'vuetify'
import { useAppStore } from '@/stores/app'

export function useThemeWatcher() {
  const appStore = useAppStore()
  const theme = useTheme()

  function updateTheme(selectedTheme: string) {
    theme.change(selectedTheme)
  }

  watch(
    () => appStore.theme,
    (selectedTheme) => updateTheme(selectedTheme),
  )

  // trigger an update on startup to get the proper theme loaded
  updateTheme(appStore.theme)
}
