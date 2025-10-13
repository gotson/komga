import type { Meta, StoryObj } from '@storybook/vue3-vite'

import AccountDetails from './details.vue'
import DialogConfirmEditInstance from '@/components/dialog/ConfirmEditInstance.vue'

const meta = {
  component: AccountDetails,
  render: (args: object) => ({
    components: { AccountDetails, DialogConfirmEditInstance },
    setup() {
      return { args }
    },
    template: '<AccountDetails /><DialogConfirmEditInstance/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {},
} satisfies Meta<typeof AccountDetails>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}
