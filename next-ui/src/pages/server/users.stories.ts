import type { Meta, StoryObj } from '@storybook/vue3-vite'

import users from './users.vue'
import { http, delay } from 'msw'
import DialogConfirm from '@/fragments/fragment/dialog/Confirm.vue'
import DialogConfirmEdit from '@/fragments/fragment/dialog/ConfirmEdit.vue'
import { response401Unauthorized } from '@/mocks/api/handlers'

const meta = {
  component: users,
  render: (args: object) => ({
    components: { users, DialogConfirm, DialogConfirmEdit },
    setup() {
      return { args }
    },
    template: '<users /><DialogConfirm/><DialogConfirmEdit/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {},
} satisfies Meta<typeof users>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const Loading: Story = {
  parameters: {
    msw: {
      handlers: [http.all('*', async () => await delay(5_000))],
    },
  },
}

export const Error: Story = {
  parameters: {
    msw: {
      handlers: [http.all('*', response401Unauthorized)],
    },
  },
}
