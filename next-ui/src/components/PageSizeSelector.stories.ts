import type { Meta, StoryObj } from '@storybook/vue3-vite'

import PageSizeSelector from './PageSizeSelector.vue'
import { expect, fn } from 'storybook/test'

const meta = {
  component: PageSizeSelector,
  render: (args: object) => ({
    components: { PageSizeSelector },
    setup() {
      return { args }
    },
    template: '<PageSizeSelector v-model="args.modelValue" v-bind="args" />',
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
    modelValue: 20,
    'onUpdate:modelValue': fn(),
  },
} satisfies Meta<typeof PageSizeSelector>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const Clicked: Story = {
  args: {},
  play: async ({ canvas, userEvent }) => {
    await expect(canvas.getByRole('button')).toBeEnabled()

    await userEvent.click(canvas.getByRole('button'))
  },
}

export const Unpaged: Story = {
  args: {
    modelValue: 'unpaged',
    allowUnpaged: true,
  },
  play: async ({ canvas, userEvent }) => {
    await expect(canvas.getByRole('button')).toBeEnabled()

    await userEvent.click(canvas.getByRole('button'))
  },
}

export const CustomSizes: Story = {
  args: {
    allowUnpaged: true,
    sizes: [1, 2, 3, 4, 5],
  },
  play: async ({ canvas, userEvent }) => {
    await expect(canvas.getByRole('button')).toBeEnabled()

    await userEvent.click(canvas.getByRole('button'))
  },
}
