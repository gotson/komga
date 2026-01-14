import type { Meta, StoryObj } from '@storybook/vue3-vite'

import ImportReadList from './readlist.vue'
import DialogConfirmEditInstance from '@/components/dialog/ConfirmEditInstance.vue'
import { delay, http } from 'msw'
import SnackQueue from '@/components/SnackQueue.vue'
import { emptyCbl, garbledCbl } from '@/mocks/api/handlers/readlists'
import { response400 } from '@/mocks/api/handlers'

const meta = {
  component: ImportReadList,
  render: (args: object) => ({
    components: { ImportReadList, DialogConfirmEditInstance, SnackQueue },
    setup() {
      return { args }
    },
    template: '<ImportReadList /><DialogConfirmEditInstance/><SnackQueue/>',
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
} satisfies Meta<typeof ImportReadList>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const Loading: Story = {
  parameters: {
    msw: {
      handlers: [http.all('*/api/*', async () => await delay(2_000))],
    },
  },
}

export const ErrorNoBooks: Story = {
  parameters: {
    msw: {
      handlers: [http.post('*/api/v1/readlists/match/comicrack', () => response400(emptyCbl))],
    },
  },
}

export const ErrorInvalidFile: Story = {
  parameters: {
    msw: {
      handlers: [http.post('*/api/v1/readlists/match/comicrack', () => response400(garbledCbl))],
    },
  },
}
