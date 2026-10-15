import {
  type BrowsingContext,
  ContextQueryParam,
  formatBrowsingContext,
  parseBrowsingContext,
  type SingleBrowsingContext,
} from '@/functions/browsing-context'
import { useRouteQuery } from '@vueuse/router'

/**
 * Provides access to the {@link:BrowsingContext} stack stored in the route query.
 */
export function useBrowsingContextFromRouteQuery() {
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

/**
 * Extracts the current {@SingleBrowsingContext} from the current route.
 */
export function useCurrentBrowsingContextFromRoute() {
  const route = useRoute()

  const context = computed<SingleBrowsingContext | undefined>(() => {
    switch (route.name) {
      case '/readlist/[id]':
        return { type: 'readList', id: route.params.id }
      case '/collection/[id]':
        return { type: 'collection', id: route.params.id }
    }
  })

  return { context }
}
