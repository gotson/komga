import { useMediaQuery } from '@vueuse/core'

export function usePrimaryInput() {
  const hasNoHover = useMediaQuery('(hover: none)')
  const hasHover = useMediaQuery('(hover: hover)')
  const hasCoarsePointer = useMediaQuery('(pointer: coarse)')
  const hasFinePointer = useMediaQuery('(pointer: fine)')

  const isTouchPrimary = computed(() => hasCoarsePointer.value || hasNoHover.value)
  const isHoverDevice = computed(() => hasHover.value && hasFinePointer.value)

  return {
    isTouchPrimary: isTouchPrimary,
    isHoverDevice: isHoverDevice,
  }
}
