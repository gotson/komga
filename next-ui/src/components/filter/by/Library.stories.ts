import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Library from './Library.vue'
import { fn } from 'storybook/test'

const meta = {
  component: Library,
  render: (args: object) => ({
    components: { Library },
    setup() {
      return { args }
    },
    template: '<Library v-model="args.modelValue"/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: 'Library filter.',
      },
    },
  },
  args: {
    'onUpdate:modelValue': fn(),
    modelValue: [],
  },
} satisfies Meta<typeof Library>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const InitialValue: Story = {
  args: {
    modelValue: [
      { i: 'e', v: '1' },
      { i: 'i', v: '2' },
    ],
  },
}
