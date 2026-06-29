import type { Meta, StoryObj } from '@storybook/vue3-vite'

import CollectionCard from './CollectionCard.vue'
import { fn } from 'storybook/test'

import { userRegular } from '@/mocks/api/handlers/users'
import DialogConfirmEditInstance from '@/components/dialog/ConfirmEditInstance.vue'
import DialogConfirmInstance from '@/components/dialog/ConfirmInstance.vue'
import { mockCollection } from '@/mocks/api/handlers/collections'
import { handleGetCurrentUser } from '@/generated/openapi/msw.gen'

import { response200OK } from '@/mocks/api/utils'

const meta = {
  component: CollectionCard,
  render: (args: object) => ({
    components: { CollectionCard, DialogConfirmEditInstance, DialogConfirmInstance },
    setup() {
      return { args }
    },
    template:
      '<CollectionCard v-bind="args" /><DialogConfirmEditInstance/><DialogConfirmInstance/>',
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
    collection: mockCollection,
    onSelection: fn(),
  },
} satisfies Meta<typeof CollectionCard>

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
      handlers: [handleGetCurrentUser(() => response200OK(userRegular))],
    },
  },
  play: ({ canvas, userEvent }) => {
    userEvent.hover(canvas.getByRole('img'))
  },
}
