import type { Meta, StoryObj } from '@storybook/vue3-vite'

import DeletionWarning from './DeletionWarning.vue'

const meta = {
  component: DeletionWarning,
  render: (args: object) => ({
    components: { DeletionWarning },
    setup() {
      return { args }
    },
    template: '<DeletionWarning />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {},
} satisfies Meta<typeof DeletionWarning>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}
