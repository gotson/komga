import type { Meta, StoryObj } from '@storybook/vue3-vite'

import ApiKeys from './api-keys.vue'
import DialogConfirm from '@/fragments/fragment/dialog/Confirm.vue'

const meta = {
  component: ApiKeys,
  render: (args: object) => ({
    components: { ApiKeys, DialogConfirm },
    setup() {
      return { args }
    },
    template: '<ApiKeys /><DialogConfirm/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {},
} satisfies Meta<typeof ApiKeys>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}
