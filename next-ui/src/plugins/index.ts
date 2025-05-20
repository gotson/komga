/**
 * plugins/index.ts
 *
 * Automatically included in `./src/main.ts`
 */

// Plugins
import vuetify from './vuetify'
import pinia from '../stores'
import router from '../router'
import {PiniaColada} from '@pinia/colada'
import { PiniaColadaAutoRefetch } from '@pinia/colada-plugin-auto-refetch'

// Types
import type {App} from 'vue'

// Navigation guards
import {useLoginGuard} from '@/router/login-guard'
import {useRoleGuard} from '@/router/role-guard.ts'

export function registerPlugins(app: App) {
  app
    .use(vuetify)
    // .use(DataLoaderPlugin, {router})
    .use(router)
    .use(pinia)
    .use(PiniaColada, {
      plugins: [
        PiniaColadaAutoRefetch()
      ]
    })

  // register navigation guards
  useLoginGuard(router)
  useRoleGuard(router)
}
