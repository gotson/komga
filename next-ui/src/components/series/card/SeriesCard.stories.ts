import type { Meta, StoryObj } from '@storybook/vue3-vite'

import SeriesCard from './SeriesCard.vue'
import { mockSeries1 } from '@/mocks/api/handlers/series'
import { fn } from 'storybook/test'
import { httpTyped } from '@/mocks/api/httpTyped'
import { userRegular } from '@/mocks/api/handlers/users'
import DialogConfirmEditInstance from '@/components/dialog/ConfirmEditInstance.vue'
import DialogConfirmInstance from '@/components/dialog/ConfirmInstance.vue'

const meta = {
  component: SeriesCard,
  render: (args: object) => ({
    components: { SeriesCard, DialogConfirmEditInstance, DialogConfirmInstance },
    setup() {
      return { args }
    },
    template: '<SeriesCard v-bind="args" /><DialogConfirmEditInstance/><DialogConfirmInstance/>',
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
} satisfies Meta<typeof SeriesCard>

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

export const Hover: Story = {
  args: {},
  play: ({ canvas, userEvent }) => {
    userEvent.hover(canvas.getByRole('img'))
  },
}

export const HoverNonAdmin: Story = {
  args: {},
  parameters: {
    msw: {
      handlers: [
        httpTyped.get('/api/v2/users/me', ({ response }) => response(200).json(userRegular)),
      ],
    },
  },
  play: ({ canvas, userEvent }) => {
    userEvent.hover(canvas.getByRole('img'))
  },
}
