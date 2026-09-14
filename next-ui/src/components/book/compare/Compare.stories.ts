import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Compare from './Compare.vue'
import {
  mockTransientBookAnalyzed1,
  mockTransientBookAnalyzed2,
  mockTransientBookAnalyzed3,
  mockTransientBookAnalyzed4,
} from '@/mocks/api/handlers/transient-books'
import { transientBookDtoToBookDetails } from '@/types/BookDetails'

const meta = {
  component: Compare,
  render: (args: object) => ({
    components: { Compare },
    setup() {
      return { args }
    },
    template: '<Compare />',
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
} satisfies Meta<typeof Compare>

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

export const SomePages: Story = {
  args: {
    left: transientBookDtoToBookDetails(mockTransientBookAnalyzed1),
    right: transientBookDtoToBookDetails(mockTransientBookAnalyzed3),
  },
}

export const NoPages: Story = {
  args: {
    left: transientBookDtoToBookDetails(mockTransientBookAnalyzed1),
    right: transientBookDtoToBookDetails(mockTransientBookAnalyzed2),
  },
}
