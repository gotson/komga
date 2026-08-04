import type { Meta, StoryObj } from '@storybook/vue3-vite'

import settings from './settings.vue'
import SnackQueue from '@/components/SnackQueue.vue'
import { http, delay } from 'msw'

import { response401Unauthorized } from '@/mocks/api/utils'

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

  beforeEach({ msw }) {
    msw.use(http.patch('*/api/v1/settings', response401Unauthorized))
  },
}

export const Loading: Story = {
  beforeEach({ msw }) {
    msw.use(http.all('*/api/v1/settings', async () => await delay(5_000)))
  },
}

export const Error: Story = {
  beforeEach({ msw }) {
    msw.use(http.all('*/api/v1/settings', response401Unauthorized))
  },
}
