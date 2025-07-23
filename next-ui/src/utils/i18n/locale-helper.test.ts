import { afterEach, beforeEach, describe, expect, test, vi } from 'vitest'
import { loadLocale, fallbackLocale, setLocale, getLocale, availableLocales } from './locale-helper'

describe('locale', () => {
  beforeEach(() => {
    // mock the available locales, as locales are checked against what's available
    vi.mock('@/i18n?dir2json&ext=.json&1', () => {
      return {
        default: {
          en: {
            sample: 'sample',
          },
          fr: {
            sample: 'échantillon',
          },
        } as Record<string, Record<string, string>>,
      }
    })
  })

  afterEach(() => {
    localStorage.clear()
  })

  test('given available locales when getting available locales then they are returned', () => {
    expect(Object.keys(availableLocales)).toStrictEqual(['en', 'fr'])
  })

  test('when trying to load unknown locale then fallback locale is loaded', () => {
    const localeFallback = loadLocale(fallbackLocale)
    const localeUnknown = loadLocale('unknown')

    expect(localeUnknown).toBe(localeFallback)
  })

  test('when setting locale then it is persisted', () => {
    const spy = vi.fn(() => {})
    vi.spyOn(window, 'location', 'get').mockReturnValue({
      reload: spy,
    } as unknown as Location)

    setLocale('fr')
    const newLocale = getLocale()

    expect(newLocale).toBe('fr')
    expect(spy).toHaveBeenCalledOnce()
  })

  test('given browser preferred languages in available locales when getting locale then preferred language is used', () => {
    vi.spyOn(navigator, 'languages', 'get').mockReturnValue(['fr-XX', 'zh'])

    const locale = getLocale()

    expect(locale).toBe('fr')
  })

  test('given browser preferred languages not in available locales when getting locale then fallback locale is used', () => {
    vi.spyOn(navigator, 'languages', 'get').mockReturnValue(['ja-JP', 'zh-CN'])

    const locale = getLocale()

    expect(locale).toBe(fallbackLocale)
  })
})
