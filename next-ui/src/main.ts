/**
 * main.ts
 *
 * Bootstraps Vuetify and other plugins then mounts the App`
 */

// Plugins
import { registerPlugins } from '@/plugins'
import 'virtual:uno.css'

// Components
import App from './App.vue'

// Composables
import { createApp } from 'vue'
import { initLogger } from '@/services/logtape'

await initLogger()

const app = createApp(App)

registerPlugins(app)

app.mount('#app')
