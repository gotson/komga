import type { Preview } from '@storybook/vue3-vite'
import { setup } from '@storybook/vue3'
import { withVuetifyTheme } from './withVuetifyTheme.decorator'

import { initialize, mswLoader } from 'msw-storybook-addon'
import { handlers } from '@/mocks/api/handlers'
import { vuetify, vuetifyRulesPlugin } from '@/plugins/vuetify'
import { createPinia } from 'pinia'
import { PiniaColada } from '@pinia/colada'
import { PiniaColadaAutoRefetch } from '@pinia/colada-plugin-auto-refetch'
import { vueIntl } from '@/plugins/vue-intl'
import 'virtual:uno.css'
import { availableLocales } from '@/utils/i18n/locale-helper'
import { localeDecorator } from './locale.decorator'

initialize(
  {
    onUnhandledRequest: 'bypass',
  },
  handlers,
)

const locales: object[] = []
Object.entries(availableLocales).forEach(([code, name]) => {
  locales.push({
    value: code,
    title: name,
  })
})

const preview: Preview = {
  parameters: {
    controls: {
      matchers: {
        color: /(background|color)$/i,
        date: /Date$/i,
      },
    },

    a11y: {
      // 'todo' - show a11y violations in the test UI only
      // 'error' - fail CI on a11y violations
      // 'off' - skip a11y checks entirely
      test: 'todo',
    },
  },
  loaders: [mswLoader],
  globalTypes: {
    locale: {
      name: 'Locale',
      description: 'Internationalization locale',
      toolbar: {
        icon: 'globe',
        items: locales,
      },
    },
  },
  initialGlobals: {
    locale: 'en',
  },
}

export default preview

setup((app) => {
  // Registers your app's plugins into Storybook
  app.use(vuetify)
  app.use(vuetifyRulesPlugin)
  app.use(vueIntl)
  app.use(createPinia())
  app.use(PiniaColada, {
    plugins: [PiniaColadaAutoRefetch()],
  })
})

export const decorators = [
  withVuetifyTheme({
    // These keys are the labels that will be displayed in the toolbar theme switcher
    // The values must match the theme keys from your VuetifyOptions
    themes: {
      light: 'light',
      dark: 'dark',
    },
    defaultTheme: 'light', // The key of your default theme
  }),
  localeDecorator(),
]
