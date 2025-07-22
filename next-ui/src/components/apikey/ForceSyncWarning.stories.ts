import type { Meta, StoryObj } from '@storybook/vue3-vite'

import ForceSyncWarning from './ForceSyncWarning.vue'

const meta = {
  component: ForceSyncWarning,
  render: (args: object) => ({
    components: { ForceSyncWarning },
    setup() {
      return { args }
    },
    template: '<ForceSyncWarning />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {},
} satisfies Meta<typeof ForceSyncWarning>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}
