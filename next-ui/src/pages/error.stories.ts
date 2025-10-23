import type { Meta, StoryObj } from '@storybook/vue3-vite'

import error from './error.vue'

const meta = {
  component: error,
  render: (args: object) => ({
    components: { error },
    setup() {
      return { args }
    },
    template: '<error />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {},
} satisfies Meta<typeof error>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}
