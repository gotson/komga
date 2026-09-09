import { describe, expect, test } from 'vitest'
import { isValidLanguageCode } from '@/functions/language-code'

describe('is valid language code', () => {
  test('validate language codes', () => {
    expect(isValidLanguageCode('en-US')).toBeTruthy()
    expect(isValidLanguageCode('zh-Hant')).toBeTruthy()
    expect(isValidLanguageCode('iw-Hebr-IL')).toBeTruthy()
    expect(isValidLanguageCode('fake')).toBeFalsy()
    expect(isValidLanguageCode('zh-fake')).toBeFalsy()
  })
})
