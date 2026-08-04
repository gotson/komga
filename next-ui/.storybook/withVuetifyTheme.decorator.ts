import { h } from 'vue'
import { DecoratorHelpers } from '@storybook/addon-themes'
import StoryWrapper from './StoryWrapper.vue'
import type { StoryContext } from '@storybook/vue3-vite'

const { initializeThemeState, pluckThemeFromContext } = DecoratorHelpers

export const withVuetifyTheme = ({
  themes,
  defaultTheme,
}: {
  themes: object
  defaultTheme: string
}) => {
  initializeThemeState(Object.keys(themes), defaultTheme)

  return (storyFn: () => Component, context: StoryContext) => {
    const selectedTheme = pluckThemeFromContext(context)
    const { themeOverride } = context.parameters.themes ?? {}

    const selected = themeOverride || selectedTheme || defaultTheme

    const story = storyFn()

    return () => {
      return h(
        StoryWrapper,
        { themeName: selected }, // Props for StoryWrapper
        {
          // Puts your story into StoryWrapper's "story" slot with your story args
          story: () => h(story, { ...context.args }),
        },
      )
    }
  }
}
