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
import { currentLocale, fallbackLocale, isLocaleRtl } from '@/utils/i18n/locale-helper'
import { createRulesPlugin } from 'vuetify'
import isISBN from 'validator/es/lib/isISBN'
import isURL from 'validator/es/lib/isURL'
import { isValidLanguageCode } from '@/functions/language-code'

// load vuetify locales only for the current locale
async function loadVuetifyLocale(locale: string) {
  return await import(`../../node_modules/vuetify/lib/locale/${locale}.js`)
}
const vuetifyMessages = (await loadVuetifyLocale(currentLocale)).default

// https://vuetifyjs.com/en/introduction/why-vuetify/#feature-guides
export const vuetify = createVuetify({
  locale: {
    locale: currentLocale,
    fallback: fallbackLocale,
    rtl: {
      [currentLocale]: isLocaleRtl(),
    },
    messages: {
      [currentLocale]: vuetifyMessages,
    },
  },
  icons: {
    defaultSet: 'mdi',
    aliases,
    sets: {
      mdi,
    },
  },
  date: {
    locale: {
      en: 'en-GB',
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
})

const aliasesDefinition = {
  sameAs: (other?: string, err?: string) => {
    return (v: unknown) => other === v || err || 'Field must have the same value'
  },
  sameAsIgnoreCase: (other?: string, err?: string) => {
    return (v: unknown) =>
      other?.localeCompare(String(v), undefined, { sensitivity: 'accent' }) == 0 ||
      err ||
      'Field must have the same value'
  },
  isbn13: (err?: string) => {
    return (v: unknown) => {
      if (!v) return true
      return (typeof v === 'string' && isISBN(v, 13)) || err || 'Must be a valid ISBN 13'
    }
  },
  bcp47: (err?: string) => {
    return (v: unknown) => {
      if (!v) return true
      return (
        (typeof v === 'string' && isValidLanguageCode(v)) ||
        err ||
        'Must be a valid BCP 47 language code'
      )
    }
  },
  linkUrl: (err?: string) => {
    return (v: unknown) => {
      if (!v) return true
      return (
        (typeof v === 'string' &&
          isURL(v, {
            protocols: ['http', 'https'],
          })) ||
        err ||
        'Must be a valid URL'
      )
    }
  },
}

export const vuetifyRulesPlugin = createRulesPlugin(
  {
    aliases: aliasesDefinition,
  },
  vuetify.locale,
)

export type CustomRulesMap = typeof aliasesDefinition

export type CustomRuleTuple = {
  [K in keyof CustomRulesMap]: Parameters<CustomRulesMap[K]> extends []
    ? K
    : [K, ...Parameters<CustomRulesMap[K]>]
}[keyof CustomRulesMap]
