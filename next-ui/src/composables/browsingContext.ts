import {
  type BrowsingContext,
  ContextQueryParam,
  formatBrowsingContext,
  parseBrowsingContext,
} from '@/functions/browsing-context'
import { useRouteQuery } from '@vueuse/router'

export function useBrowsingContext() {
  // Bind directly to the route query parameter as a string
  const rawContext = useRouteQuery(ContextQueryParam)

  const context = computed({
    get() {
      return parseBrowsingContext(rawContext.value)
    },
    set(newValue?: BrowsingContext) {
      // Serialize or transform the new value back to a query string (or undefined to clear)
      rawContext.value = formatBrowsingContext(newValue)
    },
  })

  return {
    context,
  }
}
