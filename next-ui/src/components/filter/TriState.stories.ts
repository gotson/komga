import type { Meta, StoryObj } from '@storybook/vue3-vite'

import TriState from './TriState.vue'
import { fn } from 'storybook/test'

const meta = {
  component: TriState,
  render: (args: object) => ({
    components: { TriState },
    setup() {
      return { args }
    },
    template: '<TriState />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component:
          'A tri-state component used for filtering. Can also be configured as a simple checkbox.',
      },
    },
  },
  args: {
    label: 'tri state',
    'onUpdate:modelValue': fn(),
  },
} satisfies Meta<typeof TriState>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const Color: Story = {
  args: {
    color: 'primary',
  },
}

export const ValidModel: Story = {
  args: {
    modelValue: 'exclude',
  },
}

export const InvalidModel: Story = {
  args: {
    modelValue: 'nope',
  },
}

export const BiState: Story = {
  args: {
    label: 'bi state',
    triState: false,
  },
}
