import type { Meta, StoryObj } from '@storybook/vue3-vite'

import { http, delay } from 'msw'
import BuildVersion from './BuildVersion.vue'
import { releasesResponseOkNotLatest } from '@/mocks/api/handlers/releases'
import { response401Unauthorized } from '@/mocks/api/handlers'
import { httpTyped } from '@/mocks/api/httpTyped'

const meta = {
  component: BuildVersion,
  render: (args: object) => ({
    components: { BuildVersion },
    setup() {
      return { args }
    },
    template: '<BuildVersion />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {},
} satisfies Meta<typeof BuildVersion>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const OutdatedVersion: Story = {
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
      handlers: [http.all('*/api/v1/releases', async () => await delay(5_000))],
    },
  },
}

export const Error: Story = {
  parameters: {
    msw: {
      handlers: [http.all('*/api/v1/releases', response401Unauthorized)],
    },
  },
}
