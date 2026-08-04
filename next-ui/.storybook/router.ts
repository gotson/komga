import { createRouter, createMemoryHistory } from 'vue-router'
import { routes } from 'vue-router/auto-routes'
import { setupLayouts } from 'virtual:generated-layouts'

// A minimal router using createMemoryHistory with the auto-generated routes from vue-router/auto-routes,
// wrapped in setupLayouts (same as the app router).
// This resolves all named routes (e.g., /series/[id], /libraries/[viewId]/overview/[section])
// without the app's navigation guards (login/role/claim guards) that depend on auth state and Colada stores.
const router = createRouter({
  history: createMemoryHistory(),
  routes: setupLayouts(routes),
})

export default router
