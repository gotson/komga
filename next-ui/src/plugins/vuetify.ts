/**
 * plugins/vuetify.ts
 *
 * Framework documentation: https://vuetifyjs.com`
 */

// Styles
import 'vuetify/styles'
import { aliases, mdi } from 'vuetify/iconsets/mdi-unocss'

// Composables
import { createVuetify } from 'vuetify'
import { md3 } from 'vuetify/blueprints'

// Labs
import { VFileUpload } from 'vuetify/labs/VFileUpload'
import { VIconBtn } from 'vuetify/labs/VIconBtn'
import { VStepperVertical, VStepperVerticalItem } from 'vuetify/labs/VStepperVertical'
import { createRulesPlugin } from 'vuetify/labs/rules'

import { availableLocales, currentLocale, fallbackLocale } from '@/utils/i18n/locale-helper'

// load vuetify locales only for the available locales in i18n
async function loadVuetifyLocale(locale: string) {
  return await import(`../../node_modules/vuetify/lib/locale/${locale}.js`)
}

const messages: Record<string, string> = {}
void (async () => {
  for (const locale of Object.keys(availableLocales)) {
    messages[locale] = (await loadVuetifyLocale(locale)).default
  }
})()

// https://vuetifyjs.com/en/introduction/why-vuetify/#feature-guides
export const vuetify = createVuetify({
  locale: {
    locale: currentLocale,
    fallback: fallbackLocale,
    messages,
  },
  icons: {
    defaultSet: 'mdi',
    aliases,
    sets: {
      mdi,
    },
  },
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
    VFileUpload,
    VIconBtn,
    VStepperVertical,
    VStepperVerticalItem,
  },
})

export const vuetifyRulesPlugin = createRulesPlugin(
  {
    aliases: {
      sameAs: (other?: string, err?: string) => {
        return (v: unknown) => other === v || err || 'Field must have the same value'
      },
      sameAsIgnoreCase: (other?: string, err?: string) => {
        return (v: unknown) =>
          other?.localeCompare(String(v), undefined, { sensitivity: 'accent' }) == 0 ||
          err ||
          'Field must have the same value'
      },
    },
  },
  vuetify.locale,
)
