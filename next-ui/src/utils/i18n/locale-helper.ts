import localeMessages from '@/i18n?dir2json&ext=.json&lazy'
import { match } from '@formatjs/intl-localematcher'

export const fallbackLocale = 'en'

const USER_LOCALE_KEY = 'komga.userLocale'

/**
 * Loads messages from a translation file by its locale code.
 * If the translation file does not exist, loads the `fallbackLocale` instead.
 * @param locale the locale code, e.g. 'fr'
 */
export async function loadLocale(locale: string): Promise<Record<string, string>> {
  const localeToLoad = locale in availableLocales ? locale : fallbackLocale

  const lazyImports = localeMessages as unknown as Record<string, () => Promise<{ default: Record<string, string> }>>

  const loadFn = lazyImports[localeToLoad]
  if(!loadFn) return {}

  const module = await loadFn()

  return module.default || module
}

export type LocaleInfo = {
  /**
   * Name of the locale in its own locale.
   */
  nameLocalized: string
}

function loadAvailableLocales(): Record<string, LocaleInfo> {
  const localesInfo: Record<string, LocaleInfo> = {}
  Object.keys(localeMessages).forEach(
    (code) => {
      // name of the locale in that locale
      const intl = new Intl.DisplayNames([code], {type: 'language'})
      const displayName = intl.of(code) || code
      localesInfo[code] = {
        nameLocalized: displayName,
      }
    }
  )
  return localesInfo
}

/**
 * Available locales loaded from translation files.
 * Key is the locale code (e.g. 'fr')
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
 * Language display names in the current locale.
 */
export const languageDisplayNames = new Intl.DisplayNames(currentLocale, { type: 'language' })

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

export function isLocaleRtl(): boolean {
  return new Intl.Locale(currentLocale).getTextInfo().direction === 'rtl'
}
