import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Edit from './Edit.vue'
import { mockReadList1 } from '@/mocks/api/handlers/readlists'

const meta = {
  component: Edit,
  render: (args: object) => ({
    components: { Edit },
    setup() {
      return { args }
    },
    template: '<Edit v-bind="args"/>',
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
    modelValue: {
      entity: mockReadList1,
      uploadQueue: [],
      deleteQueue: [],
      selected: undefined,
    },
  },
} satisfies Meta<typeof Edit>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}
