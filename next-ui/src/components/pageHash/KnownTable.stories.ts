import type { Meta, StoryObj } from '@storybook/vue3-vite'

import KnownTable from './KnownTable.vue'
import { delay, http } from 'msw'

import { mockPage } from '@/mocks/api/pageable'
import { PageRequest } from '@/types/PageRequest'
import DialogSimpleInstance from '@/components/dialog/DialogSimpleInstance.vue'
import SnackQueue from '@/components/SnackQueue.vue'
import {
  handleDeleteDuplicatePagesByPageHash,
  handleGetKnownPageHashes,
} from '@/generated/openapi/msw.gen'
import { response200OK, response401Unauthorized } from '@/mocks/api/utils'

const meta = {
  component: KnownTable,
  subcomponents: { SnackQueue },
  render: (args: object) => ({
    components: { KnownTable, DialogSimpleInstance, SnackQueue },
    setup() {
      return { args }
    },
    template: '<KnownTable v-bind="args" /><DialogSimpleInstance/><SnackQueue/>',
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
} satisfies Meta<typeof KnownTable>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const NoData: Story = {
  parameters: {
    msw: {
      handlers: [handleGetKnownPageHashes(() => response200OK(mockPage([], new PageRequest())))],
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
      handlers: [http.all('*/api/v1/page-hashes', response401Unauthorized)],
    },
  },
}

export const ErrorOnDeletion: Story = {
  parameters: {
    msw: {
      handlers: [handleDeleteDuplicatePagesByPageHash(response401Unauthorized)],
    },
  },
}
