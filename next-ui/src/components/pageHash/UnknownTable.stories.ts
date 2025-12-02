import type { Meta, StoryObj } from '@storybook/vue3-vite'

import UnknownTable from './UnknownTable.vue'
import { delay, http } from 'msw'
import { response401Unauthorized } from '@/mocks/api/handlers'
import { httpTyped } from '@/mocks/api/httpTyped'
import { mockPage } from '@/mocks/api/pageable'
import { PageRequest } from '@/types/PageRequest'
import DialogSimpleInstance from '@/components/dialog/DialogSimpleInstance.vue'
import SnackQueue from '@/components/SnackQueue.vue'

const meta = {
  component: UnknownTable,
  subcomponents: { SnackQueue },
  render: (args: object) => ({
    components: { UnknownTable, DialogSimpleInstance, SnackQueue },
    setup() {
      return { args }
    },
    template: '<UnknownTable v-bind="args" /><DialogSimpleInstance/><SnackQueue/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {},
} satisfies Meta<typeof UnknownTable>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const NoData: Story = {
  parameters: {
    msw: {
      handlers: [
        httpTyped.get('/api/v1/page-hashes/unknown', ({ response }) =>
          response(200).json(mockPage([], new PageRequest())),
        ),
      ],
    },
  },
}

export const Loading: Story = {
  parameters: {
    msw: {
      handlers: [http.all('*/api/*', async () => await delay(2_000))],
    },
  },
}

export const Error: Story = {
  parameters: {
    msw: {
      handlers: [http.all('*/api/v1/page-hashes/unknown', response401Unauthorized)],
    },
  },
}
