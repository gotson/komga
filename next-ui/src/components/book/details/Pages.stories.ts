import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Pages from './Pages.vue'
import { mockTransientBookAnalyzed3 } from '@/mocks/api/handlers/transient-books'
import { transientBookDtoToBookDetails } from '@/types/BookDetails'
import { fn } from 'storybook/test'

const meta = {
  component: Pages,
  render: (args: object) => ({
    components: { Pages },
    setup() {
      return { args }
    },
    template: '<Pages />',
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
    onGoToPage: fn(),
  },
} satisfies Meta<typeof Pages>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}
