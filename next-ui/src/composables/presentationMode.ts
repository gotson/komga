import { useAppStore } from '@/stores/app'
import type { PresentationMode } from '@/types/libraries'
import { useDisplay } from 'vuetify'

/**
 * Provide easy access to presentation mode for a specific view.
 * Presentation mode changes to `grid` on `xs` displays.
 */
export function usePresentationMode(
  viewName: MaybeRefOrGetter<string>,
  defaultView: PresentationMode = 'grid',
) {
  const appStore = useAppStore()
  const display = useDisplay()

  const presentationMode = appStore.getPresentationMode(toValue(viewName), defaultView)
  const presentationModeEffective = computed(() =>
    display.xs.value ? 'grid' : presentationMode.value,
  )

  return {
    /**
     * The chosen presentation mode.
     */
    presentationMode: presentationMode,
    /**
     * The effective presentation mode.
     */
    presentationModeEffective: presentationModeEffective,
  }
}
