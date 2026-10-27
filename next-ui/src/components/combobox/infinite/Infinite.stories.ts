import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Infinite from './Infinite.vue'
import { expect, waitFor } from 'storybook/test'

const meta = {
  component: Infinite,
  render: (args: object) => ({
    components: { Infinite },
    setup() {
      return { args }
    },
    template: '<Infinite v-bind="args"/>',
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
    modelValue: [],
    items: ['Gaming', 'Programming', 'Vue', 'Vuetify'],
    label: 'text-input',
  },
} satisfies Meta<typeof Infinite>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const ClickOutside: Story = {
  play: async ({ canvas, userEvent }) => {
    const combobox = canvas.getByLabelText('text-input')
    await userEvent.type(combobox, 'example')

    await expect(combobox).toHaveValue('example')

    // click outside
    await userEvent.click(document.body)

    await waitFor(() => expect(combobox).toHaveValue(''))
  },
}

export const PressEsc: Story = {
  play: async ({ canvas, userEvent }) => {
    const combobox = canvas.getByLabelText('text-input')
    await userEvent.type(combobox, 'example')

    await expect(combobox).toHaveValue('example')

    // press escape key
    await userEvent.keyboard('{Escape}')

    await waitFor(() => expect(combobox).toHaveValue(''))
  },
}
