import type { Meta, StoryObj } from '@storybook/vue3-vite'

import StepGeneral from './StepGeneral.vue'

const meta = {
  component: StepGeneral,
  render: (args: object) => ({
    components: { StepGeneral },
    setup() {
      return { args }
    },
    template: '<StepGeneral :model-value="args.modelValue" v-bind="args"/>',
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
} satisfies Meta<typeof StepGeneral>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    modelValue: {
      name: '',
      root: '',
    },
  },
}
