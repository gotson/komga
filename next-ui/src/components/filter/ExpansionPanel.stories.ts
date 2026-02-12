import type { Meta, StoryObj } from '@storybook/vue3-vite'

import ExpansionPanel from './ExpansionPanel.vue'
import { VExpansionPanels } from 'vuetify/components'
import { expect, fn, waitFor } from 'storybook/test'

const meta = {
  component: ExpansionPanel,
  render: (args: object) => ({
    components: { ExpansionPanel, VExpansionPanels },
    setup() {
      return { args }
    },
    template:
      '<v-expansion-panels><ExpansionPanel v-bind="args">Slot content</ExpansionPanel></v-expansion-panels>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component:
          'A predefined `v-expansion-panel` which should be used insed a `v-expansion-panels`.',
      },
    },
  },
  args: {
    onClear: fn(),
    title: 'Default title',
  },
} satisfies Meta<typeof ExpansionPanel>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const Title: Story = {
  args: {
    title: 'Custom title',
  },
}

export const Count: Story = {
  args: {
    count: 25,
  },
}

export const Reset: Story = {
  args: {
    count: 25,
  },
  play: async ({ canvas, userEvent, args }) => {
    const chip = canvas.getByRole('button', { name: /close/i })
    await userEvent.click(chip)

    await expect(args.onClear).toHaveBeenCalledOnce()
  },
}
