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
    template: '<TriState v-model="args.modelValue"/>',
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
    modelValue: undefined,
    sortOption: {
      label: 'Title',
      key: 'title',
      initialOrder: 'asc',
      invertible: true,
    },
    'onUpdate:modelValue': fn(),
    onChange: fn(),
  },
} satisfies Meta<typeof TriState>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const Mandatory: Story = {
  args: {
    mandatory: true,
  },
}

export const InitialValueColor: Story = {
  args: {
    color: 'primary',
    modelValue: { key: 'title', order: 'asc' },
  },
}

export const BiState: Story = {
  args: {
    sortOption: {
      label: 'Title',
      key: 'title',
      initialOrder: 'asc',
      invertible: false,
    },
  },
}

export const BiStateMandatory: Story = {
  args: {
    sortOption: {
      label: 'Title',
      key: 'title',
      initialOrder: 'asc',
      invertible: false,
    },
    mandatory: true,
  },
}

export const InitialOrder: Story = {
  args: {
    sortOption: {
      label: 'Title',
      key: 'title',
      initialOrder: 'desc',
      invertible: false,
    },
  },
}

export const Number: Story = {
  args: {
    number: 2,
    color: 'primary',
    modelValue: { key: 'title', order: 'asc' },
  },
}

export const NumberNoSort: Story = {
  args: {
    number: 2,
  },
}
