import type { Meta, StoryObj } from '@storybook/vue3-vite'

import BuildCommit from './BuildCommit.vue'
import { http, HttpResponse, delay } from 'msw'
import { baseUrl, response401Unauthorized } from '@/mocks/api/handlers/base'
import { actuatorResponseOk } from '@/mocks/api/handlers/actuator'

const meta = {
  component: BuildCommit,
  render: (args: object) => ({
    components: { BuildCommit },
    setup() {
      return { args }
    },
    template: '<BuildCommit />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {},
} satisfies Meta<typeof BuildCommit>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const Loading: Story = {
  parameters: {
    msw: {
      handlers: [
        http.get(baseUrl + 'actuator/info', async () => {
          await delay(5_000)
          return HttpResponse.json(actuatorResponseOk)
        }),
      ],
    },
  },
}

export const Error: Story = {
  parameters: {
    msw: {
      handlers: [http.get(baseUrl + 'actuator/info', response401Unauthorized)],
    },
  },
}
