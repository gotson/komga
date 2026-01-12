import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Confirm from './Confirm.vue'
import { fn } from 'storybook/test'

const meta = {
  component: Confirm,
  render: (args: object) => ({
    components: { Confirm },
    setup() {
      return { args }
    },
    template: '<Confirm v-bind="args"/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: '',
      },
    },
  },
  args: {
    dialog: true,
    onConfirm: fn(),
  },
} satisfies Meta<typeof Confirm>

export default meta
type Story = StoryObj<typeof meta>

export const Click: Story = {
  args: {
    ...meta.args,
    mode: 'click',
  },
}

export const Checkbox: Story = {
  args: {
    ...meta.args,
    mode: 'checkbox',
    color: 'warning',
  },
}

export const TextInput: Story = {
  args: {
    ...meta.args,
    mode: 'textinput',
  },
}

export const TextInputCustomText: Story = {
  args: {
    ...meta.args,
    title: 'dialog title',
    subtitle: 'dialog subtitle',
    validateText: "let's go",
    mode: 'textinput',
  },
}
