import type { Meta, StoryObj } from '@storybook/vue3-vite'

import { http, HttpResponse, delay } from 'msw'
import { baseUrl, response401Unauthorized } from '@/mocks/api/handlers/base'
import BuildVersion from './BuildVersion.vue'
import { releasesResponseOk, releasesResponseOkNotLatest } from '@/mocks/api/handlers/releases'

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
        http.get(baseUrl + 'api/v1/releases', () => HttpResponse.json(releasesResponseOkNotLatest)),
      ],
    },
  },
}

export const Loading: Story = {
  parameters: {
    msw: {
      handlers: [
        http.get(baseUrl + 'api/v1/releases', async () => {
          await delay(5_000)
          return HttpResponse.json(releasesResponseOk)
        }),
      ],
    },
  },
}

export const Error: Story = {
  parameters: {
    msw: {
      handlers: [
        http.get(baseUrl + 'actuator/info', response401Unauthorized),
        http.get(baseUrl + 'api/v1/releases', response401Unauthorized),
      ],
    },
  },
}
