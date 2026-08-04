import type { Meta, StoryObj } from '@storybook/vue3-vite'

import ReadStatus from './ReadStatus.vue'
import { fn } from 'storybook/test'

const meta = {
  component: ReadStatus,
  render: (args: object) => ({
    components: { ReadStatus },
    setup() {
      return { args }
    },
    template: '<ReadStatus v-model="args.modelValue"/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: 'Read Status filter.',
      },
    },
  },
  args: {
    'onUpdate:modelValue': fn(),
    modelValue: [],
  },
} satisfies Meta<typeof ReadStatus>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const InitialValue: Story = {
  args: {
    modelValue: [
      { i: 'e', v: 'UNREAD' },
      { i: 'i', v: 'IN_PROGRESS' },
    ],
  },
}
