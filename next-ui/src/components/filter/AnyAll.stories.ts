import type { Meta, StoryObj } from '@storybook/vue3-vite'

import AnyAll from './AnyAll.vue'
import { fn } from 'storybook/test'

const meta = {
  component: AnyAll,
  render: (args: object) => ({
    components: { AnyAll },
    setup() {
      return { args }
    },
    template: '<AnyAll />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: 'Selector for how multiple conditions are applied.',
      },
    },
  },
  args: {
    'onUpdate:modelValue': fn(),
  },
} satisfies Meta<typeof AnyAll>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}
export const Props: Story = {
  args: {
    props: { rounded: false, color: 'red' },
  },
}

export const TextAndIcon: Story = {
  args: {
    text: true,
    icons: true,
  },
}

export const TextOnly: Story = {
  args: {
    text: true,
    icons: false,
  },
}

export const InitialValue: Story = {
  args: {
    modelValue: 'allOf',
  },
}
