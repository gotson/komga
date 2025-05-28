/**
 * plugins/vuetify.ts
 *
 * Framework documentation: https://vuetifyjs.com`
 */

// Styles
import '@mdi/font/css/materialdesignicons.css'
import 'vuetify/styles'

// Composables
import {createVuetify} from 'vuetify'
import {md3} from 'vuetify/blueprints'

// Labs
import {VIconBtn} from 'vuetify/labs/components'
import {createRulesPlugin} from 'vuetify/labs/rules'

// https://vuetifyjs.com/en/introduction/why-vuetify/#feature-guides
export const vuetify = createVuetify({
  theme: {
    defaultTheme: 'light',
    themes: {
      light: {
        dark: false,
        colors: {
          primary: '#005ed3',
          secondary: '#fec000',
          accent: '#ff0335',
        },
      },
      dark: {
        dark: true,
        colors: {
          primary: '#78baec',
          secondary: '#fec000',
          accent: '#ff0335',
        },
      },
    },
  },
  blueprint: md3,
  components: {
    VIconBtn,
  },
})

export const vuetifyRulesPlugin = createRulesPlugin({
  aliases: {
    sameAs: (other?: string, err?: string) => {
      return (v: unknown) => other === v || err || 'Field must have the same value'
    },
  },
}, vuetify.locale)
