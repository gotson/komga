import { defineMessage } from 'vue-intl'
import localeMessages from '@/i18n?dir2json&ext=.json&1'
import { match } from '@formatjs/intl-localematcher'

export const fallbackLocale = 'en'

const USER_LOCALE_KEY = 'komga.userLocale'

const localeName = defineMessage({
  description:
    "The name of the locale, shown in the language selection menu. Must be translated to the language's name",
  defaultMessage: 'English',
  id: 'app.locale-name',
})

/**
 * Loads messages from a translation file by its locale code.
 * If the translation file does not exist, loads the `fallbackLocale` instead.
 * @param locale the locale code, e.g. 'fr'
 */
export function loadLocale(locale: string): Record<string, string> {
  const localeToLoad = locale in availableLocales ? locale : fallbackLocale
  return (localeMessages as unknown as Record<string, Record<string, string>>)[localeToLoad]!
}

function loadAvailableLocales(): Record<string, string> {
  const localesInfo: Record<string, string> = {}
  Object.keys(localeMessages).forEach(
    (x) =>
      (localesInfo[x] = (localeMessages as unknown as Record<string, Record<string, string>>)[x]![
        localeName.id
      ]!),
  )
  return localesInfo
}

/**
 * Available locales loaded from translation files.
 * Key is the locale code (e.g. 'fr')
 * Value is the locale name in its own locale (e.g. 'Français')
 */
export const availableLocales = loadAvailableLocales()

/**
 * Gets the saved locale from localStorage if defined and valid.
 * Else tries to get the best matching language from the browser's preferred languages.
 * If the locale is not valid, defaults to 'fallbackLocale'.
 */
export function getLocale(): string {
  const storageLocale = localStorage.getItem(USER_LOCALE_KEY)
  if (storageLocale && storageLocale in availableLocales) return storageLocale

  // get the browser's preferred languages and see if we can match it to an available locale
  return match(navigator.languages, Object.keys(availableLocales), fallbackLocale)
}

export const currentLocale = getLocale()

/**
 * Save the locale to localStorage and reloads the window if it has changed.
 * @param locale the new locale
 */
export function setLocale(locale: string) {
  if (locale !== currentLocale) {
    localStorage.setItem(USER_LOCALE_KEY, locale)
    window.location.reload()
  }
}
