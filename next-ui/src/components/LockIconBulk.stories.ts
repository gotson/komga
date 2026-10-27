import type { Meta, StoryObj } from '@storybook/vue3-vite'

import LockIconBulk from './LockIconBulk.vue'

const meta = {
  component: LockIconBulk,
  render: (args: object) => ({
    components: { LockIconBulk },
    setup() {
      return { args }
    },
    template: '<LockIconBulk />',
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
} satisfies Meta<typeof LockIconBulk>

export default meta
type Story = StoryObj<typeof meta>

export const Locked: Story = {
  args: {
    initialCounts: [{ value: true, count: 2 }],
    totalCount: 2,
  },
}

export const Mixed: Story = {
  args: {
    initialCounts: [
      { value: true, count: 2 },
      { value: false, count: 3 },
    ],
    totalCount: 5,
  },
}

export const Unlocked: Story = {
  args: {
    initialCounts: [{ value: false, count: 3 }],
    totalCount: 3,
  },
}
