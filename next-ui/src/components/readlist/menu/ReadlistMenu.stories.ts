import type { Meta, StoryObj } from '@storybook/vue3-vite'

import ReadlistMenu from './ReadlistMenu.vue'
import { expect } from 'storybook/test'
import DialogConfirmInstance from '@/components/dialog/ConfirmInstance.vue'
import DialogConfirmEditInstance from '@/components/dialog/ConfirmEditInstance.vue'
import { mockReadList1 } from '@/mocks/api/handlers/readlists'

const meta = {
  component: ReadlistMenu,
  render: (args: object) => ({
    components: { ReadlistMenu, DialogConfirmInstance, DialogConfirmEditInstance },
    setup() {
      return { args }
    },
    template:
      '<v-icon-btn id="IDce0b073e6b2146e688c1cd32b61f3fef" icon="i-mdi:dots-vertical"/><ReadlistMenu v-bind="args" /><DialogConfirmInstance/><DialogConfirmEditInstance/>',
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
    activator: '#IDce0b073e6b2146e688c1cd32b61f3fef',
    readList: mockReadList1,
  },
  play: async ({ canvas, userEvent }) => {
    await expect(canvas.getByRole('button')).toBeEnabled()

    await userEvent.click(canvas.getByRole('button'))
  },
} satisfies Meta<typeof ReadlistMenu>

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
