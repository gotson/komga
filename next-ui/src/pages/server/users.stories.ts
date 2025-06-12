import type { Meta, StoryObj } from '@storybook/vue3-vite'

import users from './users.vue'

const meta = {
  component: users,
  render: (args: object) => ({
    components: { users },
    setup() {
      return { args }
    },
    template: '<users />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {},
  // This component will have an automatically generated docsPage entry: https://storybook.js.org/docs/writing-docs/autodocs
  tags: ['autodocs'],
} satisfies Meta<typeof users>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}
