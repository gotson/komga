import type { Meta, StoryObj } from '@storybook/vue3-vite'

import General from './General.vue'
import {
  mockTransientBookAnalyzed3,
  mockTransientBookAnalyzed4,
} from '@/mocks/api/handlers/transient-books'
import { transientBookDtoToBookDetails } from '@/types/BookDetails'

const meta = {
  component: General,
  render: (args: object) => ({
    components: { General },
    setup() {
      return { args }
    },
    template: '<General />',
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
    left: transientBookDtoToBookDetails(mockTransientBookAnalyzed3),
    right: transientBookDtoToBookDetails(mockTransientBookAnalyzed4),
  },
} satisfies Meta<typeof General>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const Upgrade: Story = {
  args: {
    isUpgrade: true,
  },
}
