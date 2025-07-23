import { createIntl } from 'vue-intl'
import { currentLocale, fallbackLocale, loadLocale } from '@/utils/i18n/locale-helper'

const messages = loadLocale(currentLocale)

export const vueIntl = createIntl({
  locale: currentLocale,
  defaultLocale: fallbackLocale,
  messages,
})
