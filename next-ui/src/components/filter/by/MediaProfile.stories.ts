import type { Meta, StoryObj } from '@storybook/vue3-vite'

import MediaProfile from './MediaProfile.vue'
import { fn } from 'storybook/test'

const meta = {
  component: MediaProfile,
  render: (args: object) => ({
    components: { MediaProfile },
    setup() {
      return { args }
    },
    template: '<MediaProfile v-model="args.modelValue"/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: 'Media profile filter.',
      },
    },
  },
  args: {
    'onUpdate:modelValue': fn(),
    modelValue: [],
  },
} satisfies Meta<typeof MediaProfile>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const InitialValue: Story = {
  args: {
    modelValue: ['DIVINA', 'PDF'],
  },
}
