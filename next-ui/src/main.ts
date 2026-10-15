import { initLogger } from '@/services/logtape'
await initLogger()

/**
 * main.ts
 *
 * Bootstraps Vuetify and other plugins then mounts the App`
 */

// dynamic imports so logtape can be initialized before top level imports
const { registerPlugins } = await import('@/plugins')
const { setupOpenapiClient } = await import('@/api/komga-client')

import 'virtual:uno.css'
import App from './App.vue'
import { createApp } from 'vue'

setupOpenapiClient()

const app = createApp(App)
registerPlugins(app)
app.mount('#app')
