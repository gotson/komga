import type { Meta, StoryObj } from '@storybook/vue3-vite'

import SeriesMenu from './SeriesMenu.vue'
import { mockSeries1 } from '@/mocks/api/handlers/series'
import { httpTyped } from '@/mocks/api/httpTyped'
import { userRegular } from '@/mocks/api/handlers/users'
import { expect } from 'storybook/test'
import DialogConfirmInstance from '@/components/dialog/ConfirmInstance.vue'
import DialogConfirmEditInstance from '@/components/dialog/ConfirmEditInstance.vue'

const meta = {
  component: SeriesMenu,
  render: (args: object) => ({
    components: { SeriesMenu, DialogConfirmInstance, DialogConfirmEditInstance },
    setup() {
      return { args }
    },
    template:
      '<v-icon-btn id="IDce0b073e6b2146e688c1cd32b61f3fef" icon="i-mdi:dots-vertical"/><SeriesMenu v-bind="args" /><DialogConfirmInstance/><DialogConfirmEditInstance/>',
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
    activator: '#IDce0b073e6b2146e688c1cd32b61f3fef',
    series: mockSeries1,
  },
  play: async ({ canvas, userEvent }) => {
    await expect(canvas.getByRole('button')).toBeEnabled()

    await userEvent.click(canvas.getByRole('button'))
  },
} satisfies Meta<typeof SeriesMenu>

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
