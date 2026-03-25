import type { Meta, StoryObj } from '@storybook/vue3-vite'

import PagingSelector from './PagingSelector.vue'
import { fn } from 'storybook/test'

const meta = {
  component: PagingSelector,
  render: (args: object) => ({
    components: { PagingSelector },
    setup() {
      return { args }
    },
    template: '<PagingSelector v-model="args.modelValue" v-bind="args" />',
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
    modelValue: 'scroll',
    'onUpdate:modelValue': fn(),
  },
} satisfies Meta<typeof PagingSelector>

export default meta
type Story = StoryObj<typeof meta>

export const Scroll: Story = {
  args: {},
}

export const Paged: Story = {
  args: {
    modelValue: 'paged',
  },
}
