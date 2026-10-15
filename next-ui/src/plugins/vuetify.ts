/**
 * plugins/vuetify.ts
 *
 * Framework documentation: https://vuetifyjs.com`
 */

// Styles
import 'vuetify/styles'
import { aliases, mdi } from 'vuetify/iconsets/mdi-unocss'

// Composables
import { createRulesPlugin, createVuetify, type LocaleMessages } from 'vuetify'
import { md3 } from 'vuetify/blueprints'
import { currentLocale, fallbackLocale, isLocaleRtl } from '@/utils/i18n/locale-helper'
import isISBN from 'validator/es/lib/isISBN'
import isURL from 'validator/es/lib/isURL'
import { isValidLanguageCode } from '@/functions/language-code'
import { getLogger } from '@logtape/logtape'

const logger = getLogger(['app', 'vuetify'])
// Glob all Vuetify locale modules so Vite pre-chunks them cleanly
const vuetifyLocaleModules = import.meta.glob<{ default: LocaleMessages }>(
  '/node_modules/vuetify/lib/locale/*.js',
)

type VuetifyLocaleResult = {
  messages: LocaleMessages
  resolvedKey: string
}

/**
 * Load vuetify locale for the provided locale string.
 * If the locale does not exist, it will try the simple language code also (like 'pt' for 'pt-BR').
 * @param locale
 */
async function loadVuetifyLocale(locale: string): Promise<VuetifyLocaleResult | null> {
  const parsed = new Intl.Locale(locale)
  const candidates = [parsed.baseName]

  // If a subtag exists (region or script), add the base language as fallback
  if (parsed.region || parsed.script) {
    candidates.push(parsed.language) // e.g., 'pt' or 'zh'
  }

  logger.debug(`locales candidates: ${candidates.join()}`)

  for (const key of candidates) {
    const path = `/node_modules/vuetify/lib/locale/${key}.js`

    if (path in vuetifyLocaleModules) {
      try {
        const loadModule = vuetifyLocaleModules[path]
        if (loadModule) {
          logger.debug(`loading module at ${path}`)
          const mod = await loadModule()
          return {
            messages: mod.default,
            resolvedKey: key,
          }
        }
      } catch (e) {
        logger.warn(`failed to load module at ${path}`, { error: e })
      }
    }
  }

  logger.warn(`no matching locale file found for "${locale}"`)
  return null
}

// concurrently load both the current and fallback locale messages
const [activeResult, fallbackResult] = await Promise.all([
  loadVuetifyLocale(currentLocale),
  currentLocale !== fallbackLocale ? loadVuetifyLocale(fallbackLocale) : null,
])

// Extract the resolved keys (falling back to raw locale strings if null)
const activeVuetifyKey = activeResult?.resolvedKey ?? currentLocale
const fallbackVuetifyKey = fallbackResult?.resolvedKey ?? fallbackLocale

// Build the messages payload safely
const messages: Record<string, string | LocaleMessages> = {}
if (activeResult) messages[activeVuetifyKey] = activeResult.messages
if (fallbackResult) messages[fallbackVuetifyKey] = fallbackResult.messages

// https://vuetifyjs.com/en/introduction/why-vuetify/#feature-guides
export const vuetify = createVuetify({
  locale: {
    // pass the resolved keys to Vuetify
    locale: activeVuetifyKey,
    fallback: fallbackVuetifyKey,
    rtl: {
      [activeVuetifyKey]: isLocaleRtl(),
    },
    messages,
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
