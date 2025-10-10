import type { Meta, StoryObj } from '@storybook/vue3-vite'

import { http, delay } from 'msw'
import Table from './Table.vue'
import { response400BadRequest } from '@/mocks/api/handlers'
import SnackQueue from '@/fragments/fragment/SnackQueue.vue'
import { matchCbl } from '@/mocks/api/handlers/readlists'
import { expect, waitFor } from 'storybook/test'

const meta = {
  component: Table,
  subcomponents: { SnackQueue },
  render: (args: object) => ({
    components: { Table, SnackQueue },
    setup() {
      return { args }
    },
    template: '<Table v-bind="args"/><SnackQueue/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {},
} satisfies Meta<typeof Table>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    match: matchCbl,
  },
}

const singleMatch = {
  ...matchCbl,
  requests: [
    {
      request: { series: ['Space Adventures (2018)', 'Space Adventures'], number: '1' },
      matches: [
        {
          series: {
            seriesId: '63',
            title: 'Space Adventures',
            releaseDate: '2018-07-10',
          },
          books: [{ bookId: '0F99E5W723ENS', number: '1', title: 'Volume 1' }],
        },
      ],
    },
  ],
}
export const Created: Story = {
  args: {
    match: singleMatch,
  },
  play: async ({ canvas, userEvent }) => {
    await userEvent.click(canvas.getByRole('button', { name: /create/i }))

    await waitFor(() => expect(canvas.getByRole('button', { name: /create/i })).toBeDisabled())
  },
  render: (args: object) => ({
    components: { Table, SnackQueue },
    setup() {
      return { args }
    },
    template: '<Table v-bind="args"/>',
  }),
}

export const Empty: Story = {
  args: {
    match: { ...matchCbl, requests: [] },
  },
}

export const Loading: Story = {
  args: {
    match: singleMatch,
  },
  parameters: {
    msw: {
      handlers: [http.all('*', async () => await delay(2_000))],
    },
  },
}

export const ErrorOnCreation: Story = {
  args: {
    match: singleMatch,
  },
  parameters: {
    msw: {
      handlers: [http.post('*/v1/readlists', response400BadRequest)],
    },
  },
}
