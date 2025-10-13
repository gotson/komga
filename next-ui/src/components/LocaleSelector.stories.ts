import type { Meta, StoryObj } from '@storybook/vue3-vite'

import LocaleSelector from './LocaleSelector.vue'
import { expect } from 'storybook/test'

const meta = {
  component: LocaleSelector,
  render: (args: object) => ({
    components: { LocaleSelector },
    setup() {
      return { args }
    },
    template: '<LocaleSelector />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {},
} satisfies Meta<typeof LocaleSelector>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const Clicked: Story = {
  args: {},
  play: async ({ canvas, userEvent }) => {
    await expect(canvas.getByRole('button')).toBeEnabled()

    await userEvent.click(canvas.getByRole('button'))
  },
}
