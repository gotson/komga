import type { Meta, StoryObj } from '@storybook/vue3-vite'

import ReadlistCard from './ReadlistCard.vue'
import { fn } from 'storybook/test'
import { httpTyped } from '@/mocks/api/httpTyped'
import { userRegular } from '@/mocks/api/handlers/users'
import DialogConfirmEditInstance from '@/components/dialog/ConfirmEditInstance.vue'
import DialogConfirmInstance from '@/components/dialog/ConfirmInstance.vue'
import { mockReadList1 } from '@/mocks/api/handlers/readlists'

const meta = {
  component: ReadlistCard,
  render: (args: object) => ({
    components: { ReadlistCard, DialogConfirmEditInstance, DialogConfirmInstance },
    setup() {
      return { args }
    },
    template: '<ReadlistCard v-bind="args" /><DialogConfirmEditInstance/><DialogConfirmInstance/>',
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
    readList: mockReadList1,
    onSelection: fn(),
  },
} satisfies Meta<typeof ReadlistCard>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
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
