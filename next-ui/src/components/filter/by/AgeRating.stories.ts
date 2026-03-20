import type { Meta, StoryObj } from '@storybook/vue3-vite'

import AgeRating from './AgeRating.vue'
import { fn } from 'storybook/test'

const meta = {
  component: AgeRating,
  render: (args: object) => ({
    components: { AgeRating },
    setup() {
      return { args }
    },
    template: '<AgeRating v-model="args.modelValue" v-bind="args"/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: 'Age rating filter.',
      },
    },
  },
  args: {
    'onUpdate:modelValue': fn(),
    modelValue: { is: undefined },
  },
} satisfies Meta<typeof AgeRating>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const InitialValue: Story = {
  args: {
    modelValue: { is: 16 },
  },
}

export const InitialValueAny: Story = {
  args: {
    modelValue: { is: 'any' },
  },
}

export const InitialValueNone: Story = {
  args: {
    modelValue: { is: 'none' },
  },
}

export const InitialRange: Story = {
  args: {
    modelValue: { min: 16, max: 18 },
  },
}

export const InitialBoth: Story = {
  args: {
    modelValue: { is: 14, min: 16, max: 18 },
  },
}
