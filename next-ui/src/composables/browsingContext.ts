import {
  ContextQueryParam,
  formatBrowsingContext,
  parseBrowsingContext,
} from '@/functions/browsing-context'

export function useBrowsingContext() {
  const route = useRoute()

  const context = computed(() => parseBrowsingContext(route.query[ContextQueryParam]))

  return {
    context,
    asString: computed(() => formatBrowsingContext(context.value)),
  }
}
