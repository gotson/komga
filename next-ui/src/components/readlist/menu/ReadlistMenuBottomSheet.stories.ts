import type { Meta, StoryObj } from '@storybook/vue3-vite'

import ReadlistMenuBottomSheet from './ReadlistMenuBottomSheet.vue'
import DialogConfirmInstance from '@/components/dialog/ConfirmInstance.vue'
import DialogConfirmEditInstance from '@/components/dialog/ConfirmEditInstance.vue'
import { mockReadList1 } from '@/mocks/api/handlers/readlists'

const meta = {
  component: ReadlistMenuBottomSheet,
  render: (args: object) => ({
    components: { ReadlistMenuBottomSheet, DialogConfirmInstance, DialogConfirmEditInstance },
    setup() {
      return { args }
    },
    template:
      '<ReadlistMenuBottomSheet v-bind="args" /><DialogConfirmInstance/><DialogConfirmEditInstance/>',
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
    modelValue: true,
    readList: mockReadList1,
  },
} satisfies Meta<typeof ReadlistMenuBottomSheet>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

// export const NonAdmin: Story = {
//   args: {},
//   parameters: {
//     msw: {
//       handlers: [
//         httpTyped.get('/api/v2/users/me', ({ response }) => response(200).json(userRegular)),
//       ],
//     },
//   },
// }
