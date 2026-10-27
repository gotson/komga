import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Sharing from './Sharing.vue'
import { mockSeries1, mockSeries2 } from '@/mocks/api/handlers/series'

const meta = {
  component: Sharing,
  render: (args: object) => ({
    components: { Sharing },
    setup() {
      return { args }
    },
    template: '<Sharing v-bind="args"/>',
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
    modelValue: { sharingLabels: [], sharingLabelsLock: undefined },
  },
} satisfies Meta<typeof Sharing>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    entities: [mockSeries1.metadata, mockSeries2.metadata],
  },
}
