import type { Meta, StoryObj } from '@storybook/vue3-vite'

import CreateEdit from './EditMetadata.vue'

const meta = {
  component: CreateEdit,
  render: (args: object) => ({
    components: { CreateEdit },
    setup() {
      return { args }
    },
    template: '<CreateEdit :model-value="args.modelValue" v-bind="args"/>',
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
} satisfies Meta<typeof CreateEdit>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}
