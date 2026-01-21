/**
 * router/index.ts
 *
 * Automatic routes for `./src/pages/*.vue`
 */

// Composables
import { createRouter, createWebHistory } from 'vue-router'
import { setupLayouts } from 'virtual:generated-layouts'
import { routes } from 'vue-router/auto-routes'

import { scrollerQuery, scrollMap } from '@/router/scroll'

const router = createRouter({
  history: createWebHistory(
    import.meta.env.PROD ? window.resourceBaseUrl : import.meta.env.BASE_URL,
  ),
  routes: setupLayouts(routes),
  scrollBehavior(to) {
    if (scrollMap.has(to.path)) {
      const scrollTo = scrollMap.get(to.path)
      return new Promise((resolve) => {
        void nextTick(() => {
          document.querySelector(scrollerQuery)?.scrollTo(0, scrollTo ?? 0)
          resolve(false)
        })
      })
    }
  },
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
