import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Visual from './Visual.vue'
import {
  mockTransientBookAnalyzed3,
  mockTransientBookAnalyzed4,
} from '@/mocks/api/handlers/transient-books'
import { transientBookDtoToBookDetails } from '@/types/BookDetails'

const meta = {
  component: Visual,
  render: (args: object) => ({
    components: { Visual },
    setup() {
      return { args }
    },
    template: '<Visual />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: '',
      },
    },
  },
  args: {
    left: transientBookDtoToBookDetails(mockTransientBookAnalyzed3).pages,
    right: transientBookDtoToBookDetails(mockTransientBookAnalyzed4).pages,
  },
} satisfies Meta<typeof Visual>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}
