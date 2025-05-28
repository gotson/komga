import {createIntl} from 'vue-intl'
import {currentLocale, defaultLocale, loadLocale} from '@/utils/locale-helper.ts'

const messages = await loadLocale(currentLocale)

export const vueIntl = createIntl({
  locale: currentLocale,
  defaultLocale: defaultLocale,
  messages
})
