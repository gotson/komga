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
    template: '<List />',
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
    'onUpdate:modelValue': fn(),
    items: [
      { title: 'Tag 1', value: '+tag1', valueExclude: '-tag1' },
      { title: 'Tag 2', value: '+tag2', valueExclude: '-tag2' },
      { title: 'Tag 3', value: '+tag3', valueExclude: '-tag3' },
      { title: 'Tag include only', value: '+tag4' },
    ],
  },
} satisfies Meta<typeof List>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const InitialValue: Story = {
  args: {
    modelValue: ['+tag1', '-tag2', 'crap'],
  },
}

export const Color: Story = {
  args: {
    color: 'red',
  },
}

export const Objects: Story = {
  args: {
    items: [
      { title: 'Tag 1', value: { include: 'tag1' }, valueExclude: { exclude: 'tag1' } },
      { title: 'Tag 2', value: { include: 'tag2' }, valueExclude: { exclude: 'tag2' } },
      { title: 'Tag 3', value: { include: 'tag3' }, valueExclude: { exclude: 'tag3' } },
      { title: 'Tag include only', value: { include: 'tag4' } },
    ],
  },
}
