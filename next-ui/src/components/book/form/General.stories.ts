import type { Meta, StoryObj } from '@storybook/vue3-vite'

import General from './General.vue'
import { mockBook } from '@/mocks/api/handlers/books'

const meta = {
  component: General,
  render: (args: object) => ({
    components: { General },
    setup() {
      return { args }
    },
    template: '<General v-bind="args"/>',
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
} satisfies Meta<typeof General>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    modelValue: mockBook.metadata,
  },
}
