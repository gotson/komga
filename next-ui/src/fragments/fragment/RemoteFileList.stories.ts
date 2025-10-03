import type { Meta, StoryObj } from '@storybook/vue3-vite'

import { http, delay } from 'msw'
import RemoteFileList from './RemoteFileList.vue'
import { response401Unauthorized } from '@/mocks/api/handlers'

const meta = {
  component: RemoteFileList,
  render: (args: object) => ({
    components: { RemoteFileList },
    setup() {
      return { args }
    },
    template: '<RemoteFileList v-bind="args"/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {},
} satisfies Meta<typeof RemoteFileList>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const PresetPath: Story = {
  args: {
    modelValue: '/comics',
  },
}

export const Loading: Story = {
  parameters: {
    msw: {
      handlers: [http.all('*', async () => await delay(2_000))],
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
