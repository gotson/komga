import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Details from './Details.vue'
import {
  mockTransientBookAnalyzed1,
  mockTransientBookAnalyzed3,
} from '@/mocks/api/handlers/transient-books'
import { transientBookDtoToBookDetails } from '@/types/BookDetails'

const meta = {
  component: Details,
  render: (args: object) => ({
    components: { Details },
    setup() {
      return { args }
    },
    template: '<Details />',
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
    book: transientBookDtoToBookDetails(mockTransientBookAnalyzed3),
  },
} satisfies Meta<typeof Details>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const NoPages: Story = {
  args: {
    book: transientBookDtoToBookDetails(mockTransientBookAnalyzed1),
  },
}
