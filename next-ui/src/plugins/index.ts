/**
 * plugins/index.ts
 *
 * Automatically included in `./src/main.ts`
 */

// Plugins
import { vuetify, vuetifyRulesPlugin } from './vuetify'
import pinia from '../stores'
import router from '../router'
import { PiniaColada, PiniaColadaQueryHooksPlugin } from '@pinia/colada'
import { PiniaColadaAutoRefetch } from '@pinia/colada-plugin-auto-refetch'
import { PiniaColadaDelay } from '@pinia/colada-plugin-delay'
import { vueIntl } from '@/plugins/vue-intl'

// Types
import type { App } from 'vue'
// Navigation guards
import { useLoginGuard } from '@/router/login-guard'
import { useRoleGuard } from '@/router/role-guard'
import { useScroll } from '@/router/scroll'
import { globalErrorHandler } from '@/colada/error-handling'

export function registerPlugins(app: App) {
  app
    .use(vuetify)
    .use(vuetifyRulesPlugin)
    .use(vueIntl)
    // .use(DataLoaderPlugin, {router})
    .use(router)
    .use(pinia)
    .use(PiniaColada, {
      plugins: [
        PiniaColadaQueryHooksPlugin({
          onError: globalErrorHandler,
        }),
        PiniaColadaAutoRefetch(),
        PiniaColadaDelay({
          delay: 200, // default delay
        }),
      ],
    })

  // register navigation guards
  useLoginGuard(router)
  useRoleGuard(router)
  useScroll(router)
}
