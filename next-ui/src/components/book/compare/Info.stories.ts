import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Info from './Info.vue'
import { mockTransientBookAnalyzed3 } from '@/mocks/api/handlers/transient-books'
import { transientBookDtoToBookDetails } from '@/types/BookDetails'

const meta = {
  component: Info,
  render: (args: object) => ({
    components: { Info },
    setup() {
      return { args }
    },
    template: '<Info />',
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
} satisfies Meta<typeof Info>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const MoreLess: Story = {
  args: {
    fileSizeCompared: 'more',
    pagesNumberCompared: 'less',
  },
}
