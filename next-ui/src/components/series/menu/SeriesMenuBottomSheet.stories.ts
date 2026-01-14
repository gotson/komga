import type { Meta, StoryObj } from '@storybook/vue3-vite'

import SeriesMenuBottomSheet from './SeriesMenuBottomSheet.vue'
import { mockSeries1 } from '@/mocks/api/handlers/series'
import { httpTyped } from '@/mocks/api/httpTyped'
import { userRegular } from '@/mocks/api/handlers/users'
import DialogConfirmInstance from '@/components/dialog/ConfirmInstance.vue'
import DialogConfirmEditInstance from '@/components/dialog/ConfirmEditInstance.vue'

const meta = {
  component: SeriesMenuBottomSheet,
  render: (args: object) => ({
    components: { SeriesMenuBottomSheet, DialogConfirmInstance, DialogConfirmEditInstance },
    setup() {
      return { args }
    },
    template:
      '<SeriesMenuBottomSheet v-bind="args" /><DialogConfirmInstance/><DialogConfirmEditInstance/>',
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
    modelValue: true,
    series: mockSeries1,
  },
} satisfies Meta<typeof SeriesMenuBottomSheet>

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

export const Unread: Story = {
  args: {
    series: {
      ...mockSeries1,
      booksCount: 5,
      booksReadCount: 0,
      booksUnreadCount: 5,
      booksInProgressCount: 0,
    },
  },
}

export const NonAdmin: Story = {
  args: {},
  parameters: {
    msw: {
      handlers: [
        httpTyped.get('/api/v2/users/me', ({ response }) => response(200).json(userRegular)),
      ],
    },
  },
}
