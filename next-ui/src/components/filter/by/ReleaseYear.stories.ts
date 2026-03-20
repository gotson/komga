import type { Meta, StoryObj } from '@storybook/vue3-vite'

import ReleaseYear from './ReleaseYear.vue'
import { fn } from 'storybook/test'

const meta = {
  component: ReleaseYear,
  render: (args: object) => ({
    components: { ReleaseYear },
    setup() {
      return { args }
    },
    template: '<ReleaseYear v-model="args.modelValue" v-bind="args"/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: 'Release year filter.',
      },
    },
  },
  args: {
    'onUpdate:modelValue': fn(),
    modelValue: { is: undefined },
  },
} satisfies Meta<typeof ReleaseYear>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const InitialValue: Story = {
  args: {
    modelValue: { is: '2016' },
  },
}

export const InitialRange: Story = {
  args: {
    modelValue: { min: '2018', max: '2020' },
  },
}

export const InitialBoth: Story = {
  args: {
    modelValue: { is: '2016', min: '2018', max: '2020' },
  },
}
