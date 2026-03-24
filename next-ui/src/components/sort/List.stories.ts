import type { Meta, StoryObj } from '@storybook/vue3-vite'

import List from './List.vue'
import { fn } from 'storybook/test'

const meta = {
  component: List,
  render: (args: object) => ({
    components: { List },
    setup() {
      return { args }
    },
    template: '<List v-model="args.modelValue"/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: 'List of tri-state choices that allows multiple selection.',
      },
    },
  },
  args: {
    modelValue: [],
    'onUpdate:modelValue': fn(),
    items: [
      { label: 'Invertible Asc', key: 'ia', initialOrder: 'asc', invertible: true },
      { label: 'Invertible Desc', key: 'id', initialOrder: 'desc', invertible: true },
      { label: 'Asc only', key: 'a', initialOrder: 'asc', invertible: false },
      { label: 'Desc only', key: 'd', initialOrder: 'desc', invertible: false },
    ],
  },
} satisfies Meta<typeof List>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const Mandatory: Story = {
  args: {
    mandatory: true,
  },
}

export const InitialValue: Story = {
  args: {
    modelValue: [{ key: 'ia', order: 'asc' }],
    color: 'red',
  },
}

export const MultiSort: Story = {
  args: {
    multiSort: true,
    modelValue: [
      { key: 'ia', order: 'asc' },
      { key: 'a', order: 'asc' },
    ],
  },
}

export const MultiSortMandatory: Story = {
  args: {
    multiSort: true,
    mandatory: true,
    color: 'primary',
    modelValue: [
      { key: 'ia', order: 'asc' },
      { key: 'a', order: 'asc' },
    ],
  },
}
