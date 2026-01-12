import type { Meta, StoryObj } from '@storybook/vue3-vite'

import BookFileDeleted from './BookFileDeleted.vue'
import { historyBookFileDeleted } from '@/mocks/api/handlers/history'

const meta = {
  component: BookFileDeleted,
  render: (args: object) => ({
    components: { BookFileDeleted },
    setup() {
      return { args }
    },
    template: '<BookFileDeleted v-bind="args" />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: '',
      },
    },
  },
  args: {},
} satisfies Meta<typeof BookFileDeleted>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    event: historyBookFileDeleted,
  },
}
