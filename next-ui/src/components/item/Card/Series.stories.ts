import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Series from './Series.vue'
import { mockSeries1 } from '@/mocks/api/handlers/series'
import { fn } from 'storybook/test'

const meta = {
  component: Series,
  render: (args: object) => ({
    components: { Series },
    setup() {
      return { args }
    },
    template: '<Series v-bind="args" />',
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
    series: mockSeries1,
    onSelection: fn(),
  },
} satisfies Meta<typeof Series>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}
export const Read: Story = {
  args: {
    series: {
      ...mockSeries1,
      booksCount: 5,
      booksReadCount: 5,
      booksUnreadCount: 0,
      booksInProgressCount: 0,
    },
  },
}

export const Oneshot: Story = {
  args: {
    series: {
      ...mockSeries1,
      oneshot: true,
    },
  },
}

export const Deleted: Story = {
  args: {
    series: {
      ...mockSeries1,
      deleted: true,
    },
  },
}

export const Selected: Story = {
  args: {
    selected: true,
  },
}
