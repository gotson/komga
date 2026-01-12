import type { Meta, StoryObj } from '@storybook/vue3-vite'

import MatchTable from './MatchTable.vue'
import { delay, http } from 'msw'
import { response401Unauthorized } from '@/mocks/api/handlers'
import { httpTyped } from '@/mocks/api/httpTyped'
import { mockPage } from '@/mocks/api/pageable'
import { PageRequest } from '@/types/PageRequest'

const meta = {
  component: MatchTable,
  render: (args: object) => ({
    components: { MatchTable },
    setup() {
      return { args }
    },
    template: '<MatchTable v-bind="args" />',
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
    modelValue: 'hash1',
  },
} satisfies Meta<typeof MatchTable>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {}

export const NoData: Story = {
  parameters: {
    msw: {
      handlers: [
        httpTyped.get('/api/v1/page-hashes/{pageHash}', ({ response }) =>
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
      handlers: [http.all('*/api/v1/page-hashes/*', response401Unauthorized)],
    },
  },
}
