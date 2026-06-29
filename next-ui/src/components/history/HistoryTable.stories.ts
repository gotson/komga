import type { Meta, StoryObj } from '@storybook/vue3-vite'

import HistoryTable from './HistoryTable.vue'
import { delay, http } from 'msw'

import { mockPage } from '@/mocks/api/pageable'
import { PageRequest } from '@/types/PageRequest'
import { handleGetHistoricalEvents } from '@/generated/openapi/msw.gen'
import { response200OK, response401Unauthorized } from '@/mocks/api/utils'

const meta = {
  component: HistoryTable,
  render: (args: object) => ({
    components: { HistoryTable },
    setup() {
      return { args }
    },
    template: '<HistoryTable v-bind="args" />',
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
} satisfies Meta<typeof HistoryTable>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const Loading: Story = {
  parameters: {
    msw: {
      handlers: [http.all('*/api/*', async () => await delay(5_000))],
    },
  },
}

export const NoData: Story = {
  parameters: {
    msw: {
      handlers: [handleGetHistoricalEvents(() => response200OK(mockPage([], new PageRequest())))],
    },
  },
}

export const Error: Story = {
  parameters: {
    msw: {
      handlers: [http.all('*/api/v1/history', response401Unauthorized)],
    },
  },
}
