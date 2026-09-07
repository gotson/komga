<template>
  <v-tooltip
    v-model="isOpen"
    v-bind="$attrs"
    :activator="computedActivator"
    :open-on-hover="computedOpenOnHover"
    :open-on-focus="computedOpenOnFocus"
    :open-on-click="computedOpenOnClick"
  >
    <template
      v-if="$slots.activator"
      #activator="{ props: activatorProps }"
    >
      <slot
        name="activator"
        :props="getActivatorProps(activatorProps)"
      />
    </template>

    <template
      v-if="$slots.default"
      #default
    >
      <slot />
    </template>
  </v-tooltip>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLongPress, useEventListener } from '@vueuse/core'
import { usePrimaryInput } from '@/composables/device'

const {
  longPressDelay = 500,
  longPressFade = 2500,
  clickFade = undefined,
  openOnHover = undefined,
  openOnFocus = undefined,
  openOnClick = undefined,
  activator = undefined,
} = defineProps<{
  longPressDelay?: number
  longPressFade?: number
  clickFade?: number | false
  openOnHover?: boolean
  openOnFocus?: boolean
  openOnClick?: boolean
  activator?: string | HTMLElement | Element | null
}>()

const isOpen = ref<boolean>(false)
const activatorRef = ref<HTMLElement | null>(null)
const instance = getCurrentInstance()
let fadeTimer: ReturnType<typeof setTimeout> | null = null

const { isHoverDevice } = usePrimaryInput()

// Honor explicit boolean props if provided; otherwise fallback to device detection defaults
const computedOpenOnHover = computed(() => openOnHover ?? isHoverDevice.value)
const computedOpenOnFocus = computed(() => openOnFocus ?? isHoverDevice.value)
const computedOpenOnClick = computed(() => openOnClick ?? false)

const triggerFade = (duration: number) => {
  if (fadeTimer) clearTimeout(fadeTimer)
  isOpen.value = true

  fadeTimer = setTimeout(() => {
    isOpen.value = false
  }, duration)
}

watch(isOpen, (val) => {
  if (!val && fadeTimer) {
    clearTimeout(fadeTimer)
    fadeTimer = null
  }
})

// Resolve parent DOM node explicitly to feed both Vuetify location positioning & onLongPress
const computedActivator = computed(() => {
  if (activator === 'parent') {
    return activatorRef.value || 'parent'
  }
  return activator ?? undefined
})

// Single click listener attached to activatorRef (works for slots, parent, & refs)
useEventListener(activatorRef, 'click', () => {
  if (clickFade !== false && clickFade !== undefined) {
    triggerFade(clickFade)
  }
})

// If activator="parent" is passed, set activatorRef to the parent DOM element on mount
onMounted(() => {
  if (activator === 'parent' && instance?.vnode.el) {
    // Traverse parentElement to ensure we grab an actual HTMLElement rather than a Comment/Text node
    let parent = (instance.vnode.el as HTMLElement).parentElement
    while (parent && parent.nodeType !== Node.ELEMENT_NODE) {
      parent = parent.parentElement
    }
    if (parent) {
      activatorRef.value = parent
    }
  }
})

// Merge Vuetify's activator ref function with our template ref for onLongPress
const getActivatorProps = (activatorProps: Record<string, unknown>) => {
  return {
    ...activatorProps,
    ref: (el: Element | ComponentPublicInstance | null) => {
      // Safely extract the raw HTMLElement whether el is a ComponentInstance or DOM Node
      if (el && '$el' in el) {
        activatorRef.value = (el.$el as HTMLElement) || null
      } else {
        activatorRef.value = (el as HTMLElement) || null
      }

      // Forward the element ref back to Vuetify's internal positional tracking
      if (typeof activatorProps.ref === 'function') {
        activatorProps.ref(el)
      }
    },
  }
}

onLongPress(
  activatorRef,
  () => {
    if (!isHoverDevice.value) {
      isOpen.value = true
      setTimeout(() => {
        isOpen.value = false
      }, longPressFade)
    }
  },
  { delay: longPressDelay },
)
</script>
