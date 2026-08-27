import type { Meta, StoryObj } from '@storybook/vue3-vite'

import General from './General.vue'

const meta = {
  component: General,
  render: (args: object) => ({
    components: { General },
    setup() {
      return { args }
    },
    template: '<General v-bind="args"/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: '',
      },
    },
  },
  args: {},
} satisfies Meta<typeof General>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    modelValue: {
      name: 'Read list manual',
      ordered: false,
      summary: `Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.

Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.

Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.`,
    },
  },
}

export const Ordered: Story = {
  args: {
    modelValue: {
      name: 'Read list ordered',
      ordered: true,
    },
  },
}
