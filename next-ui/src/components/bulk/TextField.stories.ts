import type { Meta, StoryObj } from '@storybook/vue3-vite'

import TextField from './TextField.vue'

const meta = {
  component: TextField,
  render: (args: object) => ({
    components: { TextField },
    setup() {
      return { args }
    },
    template: '<TextField/>',
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
} satisfies Meta<typeof TextField>
export default meta

type Story = StoryObj<typeof meta>

export const AllSet: Story = {
  args: {
    initialCounts: [{ value: 'Vertigo', count: 2 }],
    totalCount: 2,
  },
}

export const Mixed: Story = {
  args: {
    initialCounts: [
      { value: 'Vertigo', count: 3 },
      { value: null, count: 3 },
      { value: 'Dark Horse', count: 2 },
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
