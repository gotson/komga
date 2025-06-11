// @vitest-environment jsdom

import { beforeAll, expect, test, vi } from 'vitest'
import { loadLocale, defaultLocale, setLocale, getLocale, availableLocales } from './locale-helper'

beforeAll(() => {
  // mock the available locales, as locales are checked against what's available
  vi.mock('../i18n?dir2json&ext=.json&1', () => {
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

test('given available locales when getting available locales then they are returned', () => {
  expect(Object.keys(availableLocales)).toStrictEqual(['en', 'fr'])
})

test('when trying to load unknown locale then default locale is loaded', () => {
  const localeDefault = loadLocale(defaultLocale)
  const localeUnknown = loadLocale('unknown')

  expect(localeUnknown).toBe(localeDefault)
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
