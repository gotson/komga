import type { Meta, StoryObj } from '@storybook/vue3-vite'

import settings from './settings.vue'
import SnackQueue from '@/components/SnackQueue.vue'
import { http, delay } from 'msw'

import { response401Unauthorized } from '@/mocks/api/handlers'

const meta = {
  component: settings,
  subcomponents: { SnackQueue },
  render: (args: object) => ({
    components: { settings, SnackQueue },
    setup() {
      return { args }
    },
    template: '<settings /><SnackQueue/>',
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
} satisfies Meta<typeof settings>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const SaveFail: Story = {
  args: {},
  parameters: {
    msw: {
      handlers: [http.patch('*/api/v1/settings', response401Unauthorized)],
    },
  },
}

export const Loading: Story = {
  parameters: {
    msw: {
      handlers: [http.all('*/api/v1/settings', async () => await delay(5_000))],
    },
  },
}

export const Error: Story = {
  parameters: {
    msw: {
      handlers: [http.all('*/api/v1/settings', response401Unauthorized)],
    },
  },
}
