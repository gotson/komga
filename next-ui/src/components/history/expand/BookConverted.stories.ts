import type { Meta, StoryObj } from '@storybook/vue3-vite'

import BookConverted from './BookConverted.vue'
import { historyBookConverted } from '@/mocks/api/handlers/history'

const meta = {
  component: BookConverted,
  render: (args: object) => ({
    components: { BookConverted },
    setup() {
      return { args }
    },
    template: '<BookConverted v-bind="args" />',
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
} satisfies Meta<typeof BookConverted>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    event: historyBookConverted,
  },
}
