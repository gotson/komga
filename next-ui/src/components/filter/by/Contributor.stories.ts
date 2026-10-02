import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Contributor from './Contributor.vue'
import { fn } from 'storybook/test'

const meta = {
  component: Contributor,
  render: (args: object) => ({
    components: { Contributor },
    setup() {
      return { args }
    },
    template: '<Contributor v-model="args.modelValue"/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: 'Contributor filter.',
      },
    },
  },
  args: {
    'onUpdate:modelValue': fn(),
    modelValue: [],
  },
} satisfies Meta<typeof Contributor>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const Writer: Story = {
  args: {
    role: 'writer',
  },
}

export const NoData: Story = {
  args: {
    role: 'nodata',
  },
}

export const InitialValue: Story = {
  args: {
    modelValue: [
      { i: 'e', v: 'Author 2 (inker)' },
      { i: 'i', v: 'Author 3 (colorist)' },
      { i: 'i', v: 'Author 0 (writer)' },
    ],
  },
}

export const InitialValueOutsideShown: Story = {
  args: {
    modelValue: [{ i: 'i', v: 'Author 100 (letterer)' }],
  },
}
