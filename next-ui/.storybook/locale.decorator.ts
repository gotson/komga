import type { StoryContext } from '@storybook/vue3-vite'
import { setLocale } from '@/utils/i18n/locale-helper'

export const localeDecorator = () => {
  return (storyFn: () => Component, context: StoryContext) => {
    setLocale(context.globals.locale)
    return storyFn()
  }
}
