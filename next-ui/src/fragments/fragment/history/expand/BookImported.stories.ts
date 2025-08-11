import type { Meta, StoryObj } from '@storybook/vue3-vite'

import BookImported from './BookImported.vue'
import { historyBookImported } from '@/mocks/api/handlers/history'

const meta = {
  component: BookImported,
  render: (args: object) => ({
    components: { BookImported },
    setup() {
      return { args }
    },
    template: '<BookImported v-bind="args" />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {},
} satisfies Meta<typeof BookImported>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    event: historyBookImported,
  },
}
