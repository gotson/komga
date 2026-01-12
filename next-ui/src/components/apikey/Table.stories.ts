import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Table from './Table.vue'
import { fn } from 'storybook/test'
import { apiKeys } from '@/mocks/api/handlers/users'

const meta = {
  component: Table,
  render: (args: object) => ({
    components: { Table },
    setup() {
      return { args }
    },
    template: '<Table v-bind="args" />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: '',
      },
    },
  },
  args: {
    onAddApiKey: fn(),
    onDeleteApiKey: fn(),
    onForceSyncApiKey: fn(),
    onEnterAddApiKey: fn(),
    onEnterDeleteApiKey: fn(),
    onEnterForceSyncApiKey: fn(),
  },
} satisfies Meta<typeof Table>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    apiKeys: apiKeys,
  },
}

export const Loading: Story = {
  args: {
    loading: true,
  },
}

export const NoData: Story = {
  args: {
    apiKeys: [],
  },
}
