import type { Meta, StoryObj } from '@storybook/vue3-vite'

import announcements from './announcements.vue'
import { http, delay } from 'msw'

import { response401Unauthorized } from '@/mocks/api/handlers'
import { httpTyped } from '@/mocks/api/httpTyped'
import { announcementsAllRead } from '@/mocks/api/handlers/announcements'
import { expect, waitFor } from 'storybook/test'

const meta = {
  component: announcements,
  render: (args: object) => ({
    components: { announcements },
    setup() {
      return { args }
    },
    template: '<announcements />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {},
} satisfies Meta<typeof announcements>

export default meta
type Story = StoryObj<typeof meta>

export const Unread: Story = {
  args: {},
  play: async ({ canvas }) => {
    await waitFor(() => expect(canvas.getByText('eBook drop 2')).not.toBeNull())

    await expect(canvas.getByRole('button', { name: /mark all as read/i })).toBeEnabled()
  },
}

export const NoUnread: Story = {
  parameters: {
    msw: {
      handlers: [
        httpTyped.get('/api/v1/announcements', ({ response }) =>
          response(200).json(announcementsAllRead),
        ),
      ],
    },
  },
  play: async ({ canvas }) => {
    await waitFor(() => expect(canvas.getByText('eBook drop 2')).not.toBeNull())

    await expect(canvas.queryByRole('button', { name: /mark all as read/i })).toBeNull()
  },
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
