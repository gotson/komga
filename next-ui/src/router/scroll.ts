import type { Router } from 'vue-router'

/**
 * Stores the `scrollTop` position per `route.path`
 */
export const scrollMap = new Map<string, number>()
/**
 * Query to be used with `document.querySelector()` to get the main scroller.
 */
export const scrollerQuery = '.v-main__scroller'

/**
 * Persist the scroll position when navigating away from a page.
 * Scroll restoration is handled in the router's `scrollBehavior` function.
 */
export function useScroll(router: Router) {
  router.afterEach((to, from) => {
    if (from.meta.scrollable) {
      const scroller = document.querySelector(scrollerQuery)
      if (scroller) scrollMap.set(from.path, scroller.scrollTop)
    }
  })
}
