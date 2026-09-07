import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Tags from './Tags.vue'
import { mockBook } from '@/mocks/api/handlers/books'

const meta = {
  component: Tags,
  render: (args: object) => ({
    components: { Tags },
    setup() {
      return { args }
    },
    template: '<Tags v-bind="args"/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: '',
      },
    },
  },
  args: { modelValue: mockBook.metadata },
} satisfies Meta<typeof Tags>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}
