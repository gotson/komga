import type { Meta, StoryObj } from '@storybook/vue3-vite'

import NumberInput from './NumberInput.vue'

const meta = {
  component: NumberInput,
  render: (args: object) => ({
    components: { NumberInput },
    setup() {
      return { args }
    },
    template: '<NumberInput :min="0"/>',
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
    modelValue: undefined,
  },
} satisfies Meta<typeof NumberInput>

export default meta
type Story = StoryObj<typeof meta>

export const AllSet: Story = {
  args: {
    initialCounts: [{ value: 5, count: 2 }],
    totalCount: 2,
  },
}

export const Mixed: Story = {
  args: {
    initialCounts: [
      { value: 8, count: 2 },
      { value: null, count: 3 },
      { value: 5, count: 3 },
    ],
    totalCount: 8,
  },
}

export const AllUnset: Story = {
  args: {
    initialCounts: [{ value: null, count: 2 }],
    totalCount: 2,
  },
}
