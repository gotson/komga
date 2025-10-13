import type { Meta, StoryObj } from '@storybook/vue3-vite'

import { http, delay } from 'msw'
import TransientBooksTable from './TransientBooksTable.vue'
import { response400BadRequest } from '@/mocks/api/handlers'
import { scanned } from '@/mocks/api/handlers/transient-books'
import SnackQueue from '@/components/SnackQueue.vue'

const meta = {
  component: TransientBooksTable,
  subcomponents: { SnackQueue },
  render: (args: object) => ({
    components: { TransientBooksTable, SnackQueue },
    setup() {
      return { args }
    },
    template: '<TransientBooksTable v-bind="args"/><SnackQueue/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {},
} satisfies Meta<typeof TransientBooksTable>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    books: scanned,
  },
}

export const Empty: Story = {
  args: {},
}

export const Loading: Story = {
  args: {
    books: scanned,
  },
  parameters: {
    msw: {
      handlers: [http.all('*', async () => await delay(2_000))],
    },
  },
}

export const ErrorOnImport: Story = {
  args: {
    books: scanned,
  },
  parameters: {
    msw: {
      handlers: [http.all('*/v1/books/import', response400BadRequest)],
    },
  },
}
