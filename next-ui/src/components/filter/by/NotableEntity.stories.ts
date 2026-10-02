import type { Meta, StoryObj } from '@storybook/vue3-vite'

import NotableEntity from './NotableEntity.vue'
import { fn } from 'storybook/test'

const meta = {
  component: NotableEntity,
  render: (args: object) => ({
    components: { NotableEntity },
    setup() {
      return { args }
    },
    template: '<NotableEntity v-model="args.modelValue"/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: 'Notable Entity filter.',
      },
    },
  },
  args: {
    'onUpdate:modelValue': fn(),
    modelValue: [],
  },
} satisfies Meta<typeof NotableEntity>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const Location: Story = {
  args: {
    kind: 'location',
  },
}

export const NoData: Story = {
  args: {
    kind: 'nodata',
  },
}

export const InitialValue: Story = {
  args: {
    modelValue: [
      { i: 'e', v: 'Entity 2 (character)' },
      { i: 'i', v: 'Entity 3 (team)' },
      { i: 'i', v: 'Entity 0 (team)' },
    ],
  },
}

export const InitialValueOutsideShown: Story = {
  args: {
    modelValue: [{ i: 'i', v: 'Entity 100 (location)' }],
  },
}
