import type { Meta, StoryObj } from '@storybook/vue3-vite'

import ImportBooks from './books.vue'
import DialogConfirmEditInstance from '@/components/dialog/ConfirmEditInstance.vue'
import { delay, http } from 'msw'

const meta = {
  component: ImportBooks,
  render: (args: object) => ({
    components: { ImportBooks, DialogConfirmEdit: DialogConfirmEditInstance },
    setup() {
      return { args }
    },
    template: '<ImportBooks /><DialogConfirmEdit/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
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
      handlers: [http.all('*', async () => await delay(2_000))],
    },
  },
}
