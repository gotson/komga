/**
 * router/index.ts
 *
 * Automatic routes for `./src/pages/*.vue`
 */

import { createRouter, createWebHistory, type LocationQuery } from 'vue-router'
import { setupLayouts } from 'virtual:generated-layouts'
import { routes } from 'vue-router/auto-routes'
import { parse, stringify } from 'qs'

import { scrollerQuery, scrollMap } from '@/router/scroll'

const router = createRouter({
  history: createWebHistory(
    import.meta.env.PROD ? window.resourceBaseUrl : import.meta.env.BASE_URL,
  ),
  routes: setupLayouts(routes),
  scrollBehavior(to, from, savedPosition) {
    // ignore redundant navigations (like useRouteQuery syncing URL parameters)
    // this prevents the page from jumping when filters or tabs are clicked
    if (to.path === from.path) {
      return false
    }

    // use savedPosition has an indicator that this is a popstate navigation
    // scroll position is saved in scrollMap
    const scrollTo = savedPosition ? (scrollMap.get(to.path) ?? 0) : 0
    return new Promise((resolve) => {
      void nextTick(() => {
        document.querySelector(scrollerQuery)?.scrollTo(0, scrollTo)
        resolve(false)
      })
    })
  },
  stringifyQuery: stringify,
  parseQuery: (query: string) => parse(query) as unknown as LocationQuery,
})

// Workaround for https://github.com/vitejs/vite/issues/11804
router.onError((err, to) => {
  if (err?.message?.includes?.('Failed to fetch dynamically imported module')) {
    if (!localStorage.getItem('vuetify:dynamic-reload')) {
      console.log('Reloading page to fix dynamic import error')
      localStorage.setItem('vuetify:dynamic-reload', 'true')
      location.assign(to.fullPath)
    } else {
      console.error('Dynamic import error, reloading page did not fix it', err)
    }
  } else {
    console.error(err)
  }
})

void router.isReady().then(() => {
  localStorage.removeItem('vuetify:dynamic-reload')
})

export default router
