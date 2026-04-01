import type { Meta, StoryObj } from '@storybook/vue3-vite'

import CollectionMenuBottomSheet from './CollectionMenuBottomSheet.vue'
import DialogConfirmInstance from '@/components/dialog/ConfirmInstance.vue'
import DialogConfirmEditInstance from '@/components/dialog/ConfirmEditInstance.vue'
import { mockCollection } from '@/mocks/api/handlers/collections'

const meta = {
  component: CollectionMenuBottomSheet,
  render: (args: object) => ({
    components: { CollectionMenuBottomSheet, DialogConfirmInstance, DialogConfirmEditInstance },
    setup() {
      return { args }
    },
    template:
      '<CollectionMenuBottomSheet v-bind="args" /><DialogConfirmInstance/><DialogConfirmEditInstance/>',
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
    collection: mockCollection,
  },
} satisfies Meta<typeof CollectionMenuBottomSheet>

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
