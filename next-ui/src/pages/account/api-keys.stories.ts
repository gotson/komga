import type { Meta, StoryObj } from '@storybook/vue3-vite'

import ApiKeys from './api-keys.vue'

const meta = {
  component: ApiKeys,
  render: (args: object) => ({
    components: { ApiKeys },
    setup() {
      return { args }
    },
    template: '<ApiKeys />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {},
} satisfies Meta<typeof ApiKeys>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}
