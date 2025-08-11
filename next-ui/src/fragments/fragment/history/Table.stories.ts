import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Table from './Table.vue'
import { delay, http } from 'msw'
import { response401Unauthorized } from '@/mocks/api/handlers'
import { httpTyped } from '@/mocks/api/httpTyped'
import { mockPage } from '@/mocks/api/pageable'
import { PageRequest } from '@/types/PageRequest'

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
  },
  args: {},
} satisfies Meta<typeof Table>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const Loading: Story = {
  parameters: {
    msw: {
      handlers: [http.all('*', async () => await delay(5_000))],
    },
  },
}

export const NoData: Story = {
  parameters: {
    msw: {
      handlers: [
        httpTyped.get('/api/v1/history', ({ response }) =>
          response(200).json(mockPage([], new PageRequest())),
        ),
      ],
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
