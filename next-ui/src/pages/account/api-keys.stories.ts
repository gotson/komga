import type { Meta, StoryObj } from '@storybook/vue3-vite'

import ApiKeys from './api-keys.vue'
import DialogConfirmInstance from '@/components/dialog/ConfirmInstance.vue'

const meta = {
  component: ApiKeys,
  render: (args: object) => ({
    components: { ApiKeys, DialogConfirmInstance },
    setup() {
      return { args }
    },
    template: '<ApiKeys /><DialogConfirmInstance/>',
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
