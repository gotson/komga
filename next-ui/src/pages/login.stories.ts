import type { Meta, StoryObj } from '@storybook/vue3-vite'

import login from './login.vue'

const meta = {
  component: login,
  render: (args: object) => ({
    components: { login },
    setup() {
      return { args }
    },
    template: '<login />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {},
} satisfies Meta<typeof login>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}
