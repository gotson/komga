import type { Meta, StoryObj } from '@storybook/vue3-vite'

import MediaStatus from './MediaStatus.vue'
import { fn } from 'storybook/test'

const meta = {
  component: MediaStatus,
  render: (args: object) => ({
    components: { MediaStatus },
    setup() {
      return { args }
    },
    template: '<MediaStatus v-model="args.modelValue"/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: 'Media status filter.',
      },
    },
  },
  args: {
    'onUpdate:modelValue': fn(),
    modelValue: [],
  },
} satisfies Meta<typeof MediaStatus>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const InitialValue: Story = {
  args: {
    modelValue: ['READY', 'OUTDATED'],
  },
}
