import type { Meta, StoryObj } from '@storybook/vue3-vite'

import SeriesStatus from './SeriesStatus.vue'
import { fn } from 'storybook/test'

const meta = {
  component: SeriesStatus,
  render: (args: object) => ({
    components: { SeriesStatus },
    setup() {
      return { args }
    },
    template: '<SeriesStatus v-model="args.modelValue"/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: 'Series status filter.',
      },
    },
  },
  args: {
    'onUpdate:modelValue': fn(),
    modelValue: [],
  },
} satisfies Meta<typeof SeriesStatus>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const InitialValue: Story = {
  args: {
    modelValue: ['ENDED', 'ABANDONED'],
  },
}
