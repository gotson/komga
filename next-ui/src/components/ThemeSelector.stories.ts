import type { Meta, StoryObj } from '@storybook/vue3-vite'

import ThemeSelector from './ThemeSelector.vue'

const meta = {
  component: ThemeSelector,
  render: (args: object) => ({
    components: { ThemeSelector },
    setup() {
      return { args }
    },
    template: '<ThemeSelector />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: 'A cycling button to change the theme.',
      },
    },
  },
  args: {},
} satisfies Meta<typeof ThemeSelector>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}
