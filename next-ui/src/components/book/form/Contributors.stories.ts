import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Contributors from './Contributors.vue'
import { mockBook } from '@/mocks/api/handlers/books'

const meta = {
  component: Contributors,
  render: (args: object) => ({
    components: { Contributors },
    setup() {
      return { args }
    },
    template: '<Contributors v-bind="args"/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: '',
      },
    },
  },
  args: { modelValue: mockBook },
} satisfies Meta<typeof Contributors>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}
