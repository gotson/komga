import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Poster from './Poster.vue'
import { fn } from 'storybook/test'

const meta = {
  component: Poster,
  render: (args: object) => ({
    components: { Poster },
    setup() {
      return { args }
    },
    template: '<Poster v-model="args.modelValue"/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: 'Poster filter.',
      },
    },
  },
  args: {
    'onUpdate:modelValue': fn(),
    modelValue: [],
  },
} satisfies Meta<typeof Poster>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const InitialValue: Story = {
  args: {
    modelValue: [
      { i: 'i', v: ['GENERATED', true] },
      { i: 'e', v: ['USER_UPLOADED', false] },
    ],
  },
}
