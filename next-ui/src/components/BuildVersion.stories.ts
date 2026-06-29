import type { Meta, StoryObj } from '@storybook/vue3-vite'

import { http, delay } from 'msw'
import BuildVersion from './BuildVersion.vue'
import { releasesResponseOkNotLatest } from '@/mocks/api/handlers/releases'
import { handleGetReleases } from '@/generated/openapi/msw.gen'
import { response200OK, response401Unauthorized } from '@/mocks/api/utils'

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
    docs: {
      description: {
        component: '',
      },
    },
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
      handlers: [handleGetReleases(() => response200OK(releasesResponseOkNotLatest))],
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
