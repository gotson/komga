import type { Meta, StoryObj } from '@storybook/vue3-vite'

import updates from './updates.vue'
import { http, delay } from 'msw'

import { response401Unauthorized } from '@/mocks/api/handlers'
import { httpTyped } from '@/mocks/api/httpTyped'
import { releasesResponseOkNotLatest } from '@/mocks/api/handlers/releases'

const meta = {
  component: updates,
  render: (args: object) => ({
    components: { updates },
    setup() {
      return { args }
    },
    template: '<updates />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {},
} satisfies Meta<typeof updates>

export default meta
type Story = StoryObj<typeof meta>

export const Latest: Story = {
  args: {},
}

export const Outdated: Story = {
  parameters: {
    msw: {
      handlers: [
        httpTyped.get('/api/v1/releases', ({ response }) =>
          response(200).json(releasesResponseOkNotLatest),
        ),
      ],
    },
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
