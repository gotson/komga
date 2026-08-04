import { useMediaQuery } from '@vueuse/core'

export function usePrimaryInput() {
  const hasNoHover = useMediaQuery('(hover: none)')
  const hasCoarsePointer = useMediaQuery('(pointer: coarse)')

  const isTouchPrimary = computed(() => hasCoarsePointer.value || hasNoHover.value)

  return {
    isTouchPrimary: isTouchPrimary,
  }
}
