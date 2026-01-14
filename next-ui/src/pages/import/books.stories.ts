import type { Meta, StoryObj } from '@storybook/vue3-vite'

import ImportBooks from './books.vue'
import DialogConfirmEditInstance from '@/components/dialog/ConfirmEditInstance.vue'
import { delay, http } from 'msw'

const meta = {
  component: ImportBooks,
  render: (args: object) => ({
    components: { ImportBooks, DialogConfirmEditInstance },
    setup() {
      return { args }
    },
    template: '<ImportBooks /><DialogConfirmEditInstance/>',
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
} satisfies Meta<typeof ImportBooks>

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
