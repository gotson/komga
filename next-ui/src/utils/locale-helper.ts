import {defineMessage} from 'vue-intl'

export const defaultLocale = 'en'

const localeName = defineMessage({
  description: 'The name of the locale, shown in the language selection menu. Must be translated to the language\'s name',
  defaultMessage: 'English',
  id: 'app.locale-name'
})

/**
 * Loads messages from a translation file by its locale code.
 * If the translation file does not exist, loads the `defaultLocale` instead.
 * @param locale the locale code, e.g. 'fr'
 */
export async function loadLocale(locale: string) {
  const localeToLoad = locale in availableLocales ? locale : defaultLocale
  const { default: messages } = await import(`@/i18n/${localeToLoad}.json`);
  return messages;
}

async function loadAvailableLocales(): Promise<Record<string, string>> {
  const localeFiles = import.meta.glob('@/i18n/*.json')
  const locales: Record<string, string> = {}
  for (const path in localeFiles) {
    const matched = path.match(/([A-Za-z0-9-_]+)\./i)
    if (matched && matched.length > 1) {
      const locale = matched[1]
      const messages = await localeFiles[path]!() as Record<string, string>
      locales[locale!] = messages.default![localeName.id]!
    }
  }
  return locales
}

/**
 * Available locales loaded from translation files.
 * Key is the locale code (e.g. 'fr')
 * Value is the locale name in its own locale (e.g. 'Français')
 */
export const availableLocales = await loadAvailableLocales()

/**
 * Gets the saved locale from localStorage.
 * If the locale is not valid, defaults to 'en'.
 */
function getLocale(): string {
  const storageLocale = localStorage.getItem('userLocale') ?? defaultLocale
  return storageLocale in availableLocales ? storageLocale : defaultLocale
}

export const currentLocale = getLocale()

/**
 * Save the locale to localStorage and reloads the window if it has changed.
 * @param locale the new locale
 */
export function setLocale(locale: string) {
  if(locale !== currentLocale) {
    localStorage.setItem('userLocale', locale)
    window.location.reload()
  }
}
