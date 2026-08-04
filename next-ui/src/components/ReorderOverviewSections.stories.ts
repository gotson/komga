import type { Meta, StoryObj } from '@storybook/vue3-vite'

import ReorderOverviewSections from './ReorderOverviewSections.vue'

import { OverviewSectionsDefault } from '@/types/OverviewSection'

const meta = {
  component: ReorderOverviewSections,
  render: (args: object) => ({
    components: { ReorderOverviewSections },
    setup() {
      return { args }
    },
    template: '<ReorderOverviewSections />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: '',
      },
    },
  },
  args: {},
} satisfies Meta<typeof ReorderOverviewSections>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    modelValue: [...OverviewSectionsDefault],
  },
}

export const OnDeckOnly: Story = {
  args: {
    modelValue: [{ section: 'on_deck' }],
  },
}

export const None: Story = {
  args: {
    modelValue: [],
  },
}
