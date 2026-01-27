import type { Meta, StoryObj } from '@storybook/vue3-vite'

import PresentationSelector from './PresentationSelector.vue'
import { expect, fn } from 'storybook/test'

const meta = {
  component: PresentationSelector,
  render: (args: object) => ({
    components: { PresentationSelector },
    setup() {
      return { args }
    },
    template: '<PresentationSelector v-model="args.modelValue" v-bind="args" />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: 'A button that will display a list of presentation modes when clicked.',
      },
    },
  },
  args: {
    modelValue: 'grid',
    modes: ['grid', 'list', 'table'],
    'onUpdate:modelValue': fn(),
  },
} satisfies Meta<typeof PresentationSelector>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const Toggle: Story = {
  args: {
    toggle: true,
  },
}

export const Clicked: Story = {
  tags: ['!autodocs'],
  args: {},
  play: async ({ canvas, userEvent }) => {
    await expect(canvas.getByRole('button')).toBeEnabled()

    await userEvent.click(canvas.getByRole('button'))
  },
}

export const LimitedSet: Story = {
  args: {
    modes: ['grid', 'list'],
  },
  play: async ({ canvas, userEvent }) => {
    await expect(canvas.getByRole('button')).toBeEnabled()

    await userEvent.click(canvas.getByRole('button'))
  },
}

export const LimitedSetToggle: Story = {
  args: {
    modes: ['grid', 'list'],
    toggle: true,
  },
}
