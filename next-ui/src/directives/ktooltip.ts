import { createVNode, render, type Directive, type DirectiveBinding } from 'vue'
import KTooltip from '@/components/k/Tooltip.vue'
import { isObject } from '@/functions/object'

export type KTooltipDirectiveBinding = Omit<DirectiveBinding, 'arg' | 'value'> & {
  arg?: string
  value?: string | boolean | Record<string, unknown>
}

type TooltipData = {
  container: HTMLDivElement
  vnode: ReturnType<typeof createVNode>
}

const elementMap = new WeakMap<HTMLElement, TooltipData>()

/**
 Normalizes directive bindings into props accepted by KTooltip / VTooltip
 */
function resolveBinding(
  el: HTMLElement,
  binding: KTooltipDirectiveBinding,
): Record<string, unknown> {
  const value =
    typeof binding.value === 'boolean'
      ? { modelValue: binding.value }
      : isObject(binding.value)
        ? binding.value
        : {}

  const text =
    typeof binding.value === 'boolean' ? undefined : (binding.value ?? el.textContent?.trim())

  return {
    text,
    location: binding.arg?.replace('-', ' '),
    activator: 'parent',
    ...value,
  }
}

/**
 Creates a fresh VNode for the tooltip to ensure Vue diffing triggers updates
 */
function createTooltipVNode(el: HTMLElement, binding: KTooltipDirectiveBinding) {
  const props = resolveBinding(el, binding)

  const vnode = createVNode(KTooltip, props, {
    activator: ({ props: activatorProps }: { props: Record<string, unknown> }) => {
      if (typeof activatorProps.ref === 'function') {
        activatorProps.ref(el)
      }

      Object.keys(activatorProps).forEach((key) => {
        if (key.startsWith('on') && typeof activatorProps[key] === 'function') {
          const eventName = key.substring(2).toLowerCase()
          el.addEventListener(eventName, activatorProps[key] as EventListener)
        }
      })

      return null
    },
  })

  if (binding.instance?.$) {
    vnode.appContext = binding.instance.$.appContext
  }

  return vnode
}

export const vKtooltip: Directive<HTMLElement> = {
  mounted(el, binding: KTooltipDirectiveBinding) {
    const container = document.createElement('div')
    const vnode = createTooltipVNode(el, binding)

    render(vnode, container)
    elementMap.set(el, { container, vnode })
  },

  updated(el, binding: KTooltipDirectiveBinding) {
    const data = elementMap.get(el)
    if (!data) return

    // Create a NEW VNode with the latest data to trigger Vue's patch process
    const vnode = createTooltipVNode(el, binding)

    // Render the new VNode into the existing container
    render(vnode, data.container)

    // Update the map with the new VNode reference
    elementMap.set(el, { container: data.container, vnode })
  },

  unmounted(el) {
    const data = elementMap.get(el)
    if (!data) return

    render(null, data.container)
    elementMap.delete(el)
  },
}

export default vKtooltip
